package fudge.gui

import kotlin.math.max
import kotlin.math.min

data class NodeSize(
    val minWidth: Int,
    val maxWidth: Int,
    val minHeight: Int,
    val maxHeight: Int
) {
    init {
        require(minWidth <= maxWidth)
        require(minHeight <= maxHeight)
    }

    private fun Int.safePlus(other: Int) =
        if (this == Int.MAX_VALUE || other == Int.MAX_VALUE) Int.MAX_VALUE else this + other

    operator fun plus(other: NodeSize) = NodeSize(
        minWidth.safePlus(other.minWidth),
        maxWidth.safePlus(other.maxWidth),
        minHeight.safePlus(other.minHeight),
        maxHeight.safePlus(other.maxHeight)
    )

    companion object {
        val Anything = NodeSize(0, Int.MAX_VALUE, 0, Int.MAX_VALUE)
        val Max = NodeSize(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}

data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun constrain(constraints: Rectangle) =
        if (x < constraints.x || y < constraints.y || width > constraints.width || height > constraints.height) {
            Rectangle(
                max(x, constraints.x),
                max(y, constraints.y),
                min(width, constraints.width),
                min(height, constraints.height)
            )
        } else this
}

typealias SizeModifierCallback = (childrenSizes: List<NodeSize>) -> NodeSize

abstract class SizeModifier(private val debugName: String?) : Modifier {
    abstract fun size(childrenSizes: List<NodeSize>): NodeSize
    override fun toString(): String = debugName ?: "Custom SizeModifier"
//    val minHeight: Int?
//    val maxHeight: Int?
//    val minWidth: Int?
//    val maxWidth: Int?

    object Default : SizeModifier("SizeModifier.Default") {
        override fun size(childrenSizes: List<NodeSize>): NodeSize {
            // Make sure the parent minwidth/minheight is at least as much as the children
            val maxMinWidth = childrenSizes.maxOfOrNull { it.minWidth } ?: 0
            val maxMinHeight = childrenSizes.maxOfOrNull { it.minHeight } ?: 0
            return NodeSize(maxMinWidth, Int.MAX_VALUE, maxMinHeight, Int.MAX_VALUE)
        }
    }
}

fun Modifier.size(width: Int, height: Int) = size("SizeModifier width = $width, height = $height") {
    NodeSize(width, width, height, height)
}

fun Modifier.fillMaxSize() = size("fill max size SizeModifier") { NodeSize.Max }

fun Modifier.size(debugName: String? = null, callback: SizeModifierCallback) =
    this wrap object : SizeModifier(debugName) {
        override fun size(childrenSizes: List<NodeSize>): NodeSize = callback(childrenSizes)
    }

typealias LayoutModifierCallback = (childrenSizes: List<NodeSize>, constraints: Rectangle) -> List<Rectangle>

abstract class LayoutModifier : Modifier {
    override fun toString(): String = "LayoutModifier"
    abstract fun layoutChildren(childrenSizes: List<NodeSize>, constraints: Rectangle): List<Rectangle>
}

internal val RootLayout = Modifier.layout { childrenSizes, screenConstraints ->
    assert(childrenSizes.size == 1) { "Root node can only have one child!" }
    val childSize = childrenSizes[0]
    listOf(
        Rectangle(
            0,
            0,
            childSize.minWidth,
            childSize.minHeight
        ).constrain(screenConstraints)
    )
}

fun Modifier.layout(layoutFunc: LayoutModifierCallback) = this wrap object : LayoutModifier() {
    override fun layoutChildren(childrenSizes: List<NodeSize>, constraints: Rectangle): List<Rectangle> =
        layoutFunc(childrenSizes, constraints)
}

data class Point(val x: Int, val y: Int)

/**
 * Simpler version of [layout] that doesn't allow deciding the size of the children, instead uses the min width/height of its children.
 */
fun Modifier.positionChildren(layoutFunc: (childrenSizes: List<NodeSize>, constraints: Rectangle) -> List<Point>) =
    layout { childrenSizes, constraints ->
        val positions = layoutFunc(childrenSizes, constraints)
        require(positions.size == childrenSizes.size) {
            "A position must be supplied for all ${childrenSizes.size} children, but only ${positions.size} were supplied instead!"
        }
        positions.zip(childrenSizes).map { (pos, size) -> Rectangle(pos.x, pos.y, size.minWidth, size.minHeight).constrain(constraints) }
    }

data class Alignment(val x: Double, val y: Double) {
    init {
        require(x in 0.0..1.0)
        require(y in 0.0..1.0)
    }
}


//fun Modifier.align(alignment: Alignment)= LayoutModifier { childrenSizes, constraints ->
//
//}

internal class Placeable(val constraints: Rectangle, val node: ComposableObject, val children: List<Placeable>)

internal data class SizeTree(val size: NodeSize, val node: ComposableObject, val childrenSizes: List<SizeTree>)

/**
 *  Size trees are built from bottom to top
 *
 *     4x2       <--- Evaluated second, based on children
 *    /   \
 *  2x2  2x2     <--- Evaluated first
 *  */

internal fun ComposableObject.buildSizeTree(): SizeTree {
    val childrenSizes = children.map { it.buildSizeTree() }
    return SizeTree(sizeModifier.size(childrenSizes.map { it.size }), this, childrenSizes)
}

/**
 * Placements Are built from top to bottom, after calculating size tree.
 *
 *              [0,0] -> [4,2]            <--- Evaluated first, based on size (from the size tree) and parent constraints
 *                 /      \
 *     [0,0] -> [2,2]   [2,2] -> [4,2]    <--- Evaluated second, based on the layout the parent decided
 *  */
internal fun ComposableObject.placeChildren(sizeTree: SizeTree, constraints: Rectangle): List<Placeable> {
    if (sizeTree.childrenSizes.isEmpty()) return listOf()

    val layout = layoutModifier
        ?: error("Node $this must decide how to layout its ${sizeTree.childrenSizes.size} children via a layout modifier!")
    val childrenConstraints = layout.layoutChildren(sizeTree.childrenSizes.map { it.size }, constraints)
    return childrenConstraints.mapIndexed { i, childConstraints ->
        val sizeBranch = sizeTree.childrenSizes[i]
        Placeable(childConstraints, sizeBranch.node, sizeBranch.node.placeChildren(sizeBranch, childConstraints))
    }
}