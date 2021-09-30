package fudge.gui

import fudge.gui.compose.wrapNode
import fudge.gui.drawing.Color
import fudge.gui.drawing.DrawContext
import fudge.gui.drawing.RenderModifier
import fudge.gui.drawing.renderBorder
import fudge.gui.layout.*
import fudge.gui.layout.borderWrapperModifier

fun Modifier.border(all: Int, color: Color) = border(all, all, all, all, color)

fun Modifier.border(
    left: Int,
    right: Int,
    top: Int,
    bottom: Int,
    color: Color
) = borderWrapperModifier(left, right, top, bottom) {
    renderBorder(left, right, top, bottom, color)
}

fun Modifier.padding(left: Int, right: Int, top: Int, bottom: Int) = borderWrapperModifier(left, right, top, bottom) {}
fun Modifier.padding(all: Int) = padding(all, all, all, all)

fun Modifier.align(alignment: Align) = positionChildren { childrenSizes, constraints ->
    assert(childrenSizes.size == 1) { "Align node can only have one child!" }
    val childSize = childrenSizes[0]

    val xManeuverRoom = constraints.width - childSize.minWidth
    val yManeuverRoom = constraints.height - childSize.minHeight
    val x = constraints.x + (xManeuverRoom * alignment.x).toInt()
    val y = constraints.y + (yManeuverRoom * alignment.y).toInt()

    val childConstraint = Point(x, y)
    listOf(childConstraint)
}.wrapNode("Align")

fun Modifier.size(width: Int, height: Int) = size("SizeModifier width = $width, height = $height") {
    NodeSize(width, width, height, height)
}

fun Modifier.fillMaxSize() = size("fill max size SizeModifier") { NodeSize.Max }


typealias SizeModifierCallback = (childrenSizes: List<NodeSize>) -> NodeSize
typealias LayoutModifierCallback = (childrenSizes: List<NodeSize>, constraints: Rect) -> List<Rect>

fun Modifier.size(debugName: String? = null, callback: SizeModifierCallback) =
    this wrap object : SizeModifier(debugName) {
        override fun size(childrenSizes: List<NodeSize>): NodeSize = callback(childrenSizes)
    }

fun Modifier.layout(layoutFunc: LayoutModifierCallback) =
    this wrap object : LayoutModifier() {
    override fun layoutChildren(childrenSizes: List<NodeSize>, constraints: Rect): List<Rect> =
        layoutFunc(childrenSizes, constraints).map { it.constrain(constraints) }
}

fun Modifier.draw(render: DrawContext.() -> Unit) = this wrap RenderModifier(render)

/**
 * Simpler version of [layout] that doesn't allow deciding the size of the children, instead uses the min width/height of its children.
 */
fun Modifier.positionChildren(layoutFunc: (childrenSizes: List<NodeSize>, constraints: Rect) -> List<Point>) =
    layout { childrenSizes, constraints ->
        val positions = layoutFunc(childrenSizes, constraints)
        require(positions.size == childrenSizes.size) {
            "A position must be supplied for all ${childrenSizes.size} children, but only ${positions.size} were supplied instead!"
        }
        positions.zip(childrenSizes).map { (pos, size) -> Rect(pos.x, pos.y, size.minWidth, size.minHeight)/*.constrain(constraints)*/ }
    }