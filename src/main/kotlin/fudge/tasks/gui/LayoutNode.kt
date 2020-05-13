package fudge.tasks.gui

import fudge.tasks.gui.compose.LayoutDirection


interface LayoutNode {
    val minSize: Size
    val maxSize: Size

    val parentData : Any? get()= null

    //    val preferredSize: Size
    fun layout(bounds: Rect, layoutDirection : LayoutDirection): Element
}




