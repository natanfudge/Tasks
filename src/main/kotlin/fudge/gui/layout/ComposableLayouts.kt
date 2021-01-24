package fudge.gui.layout

import fudge.gui.*
import fudge.gui.compose.wrapNode
import fudge.gui.drawing.DrawContext

internal fun Modifier.borderWrapperModifier(
    left: Int,
    right: Int,
    top: Int,
    bottom: Int, drawCallback: DrawContext.() -> Unit
) = borderModifier(left, right, top, bottom, drawCallback).wrapNode("Border")

internal fun Modifier.borderModifier(
    left: Int,
    right: Int,
    top: Int,
    bottom: Int,
    drawCallback: DrawContext.() -> Unit
) = size("Border SizeModifier") {
    assert(it.size == 1) { "Border may only be applied on a singular child" }
    val size = it[0]
    val bonusWidth = left + right
    val bonusHeight = top + bottom
    size + NodeSize(bonusWidth, bonusWidth, bonusHeight, bonusHeight)
}.layout { _, constraints ->
    listOf(
        Rect(
            constraints.x + left,
            constraints.y + top,
            constraints.width - left - right,
            constraints.height - top - bottom
        )
    )
}.draw(drawCallback)