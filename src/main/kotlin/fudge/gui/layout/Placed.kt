package fudge.gui.layout

import fudge.gui.compose.ComposableObject

internal class Placed(val constraints: Rect, val node: ComposableObject, val children: List<Placed>)



/**
 * Placements Are built from top to bottom, after calculating size tree.
 *
 *              [0,0] -> [4,2]            <--- Evaluated first, based on size (from the size tree) and parent constraints
 *                 /      \
 *     [0,0] -> [2,2]   [2,2] -> [4,2]    <--- Evaluated second, based on the layout the parent decided
 *  */
internal fun ComposableObject.placeChildren(sizeTree: SizeTree, constraints: Rect): List<Placed> {
    if (sizeTree.childrenSizes.isEmpty()) return listOf()

    val layout = layoutModifier
        ?: error("Node $this must decide how to layout its ${sizeTree.childrenSizes.size} children via a layout modifier!")
    val childrenConstraints = layout.layoutChildren(sizeTree.childrenSizes.map { it.size }, constraints)
    return childrenConstraints.mapIndexed { i, childConstraints ->
        val sizeBranch = sizeTree.childrenSizes[i]
        Placed(childConstraints, sizeBranch.node, sizeBranch.node.placeChildren(sizeBranch, childConstraints))
    }
}