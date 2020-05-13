package fudge.tasks.gui.compose

import fudge.tasks.gui.Element
import fudge.tasks.gui.height
import fudge.tasks.gui.width

/**
 * Read-only wrapper over [Placeable] that exposes the measurement result with no placing ability.
 */
class Measured(internal val placeable: Element) {
    /**
     * The measured width of the layout.
     */
    val width: Int get() = placeable.bounds.width

    /**
     * The measured height of the layout.
     */
    val height: Int get() = placeable.bounds.height

    /**
     * Returns the position of an [alignment line][AlignmentLine],
     * or `null` if the line is not provided.
     */
    //TODO
//    operator fun get(alignmentLine: AlignmentLine): Int? = placeable[alignmentLine]
}