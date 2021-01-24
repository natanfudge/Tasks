package fudge.gui.layout

import fudge.gui.*
import fudge.gui.compose.ComposableObject

internal object Layouts {
    val Root = Modifier.layout { childrenSizes, screenConstraints ->
        assert(childrenSizes.size == 1) { "Root node can only have one child!" }
        val childSize = childrenSizes[0]
        listOf(
            Rect(
                0,
                0,
                childSize.minWidth,
                childSize.minHeight
            ).constrain(screenConstraints)
        )
    }
}

internal object Layout {
    fun layoutGuiTree(root: ComposableObject, rootConstraints: Rect): Placed {
        val sizeTree = root.buildSizeTree()
        return Placed(
            constraints = rootConstraints,
            children = root.placeChildren(sizeTree, rootConstraints),
            node = root
        )
    }
}

abstract class LayoutModifier : Modifier {
    override fun toString(): String = "LayoutModifier"
    abstract fun layoutChildren(childrenSizes: List<NodeSize>, constraints: Rect): List<Rect>
}

abstract class SizeModifier(private val debugName: String?) : Modifier {
    abstract fun size(childrenSizes: List<NodeSize>): NodeSize
    override fun toString(): String = debugName ?: "Custom SizeModifier"

    object Default : SizeModifier("SizeModifier.Default") {
        override fun size(childrenSizes: List<NodeSize>): NodeSize {
            // Make sure the parent minwidth/minheight is at least as much as the children
            val maxMinWidth = childrenSizes.maxOfOrNull { it.minWidth } ?: 0
            val maxMinHeight = childrenSizes.maxOfOrNull { it.minHeight } ?: 0
            return NodeSize(maxMinWidth, Int.MAX_VALUE, maxMinHeight, Int.MAX_VALUE)
        }
    }
}