package fudge.gui.drawing

import fudge.gui.Modifier

fun interface RenderModifier : Modifier {
    fun DrawContext.render()
}