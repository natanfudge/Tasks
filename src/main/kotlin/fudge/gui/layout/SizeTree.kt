package fudge.gui.layout

import fudge.gui.compose.ComposableObject


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