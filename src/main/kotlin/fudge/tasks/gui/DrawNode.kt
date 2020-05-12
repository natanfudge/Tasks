package fudge.tasks.gui


interface DrawContext : Canvas {
    val size: Size
}

class DrawContextImpl(override val size: Size, val canvas: Canvas) : Canvas by canvas, DrawContext

interface DrawNode {
    fun DrawContext.draw()
    companion object
}

inline operator fun DrawNode.Companion.invoke(crossinline draw : DrawContext.() -> Unit) = object  : DrawNode{
    override fun DrawContext.draw() {
        draw()
    }
}


fun textDrawNode(text : String, color: Color) = DrawNode {
    drawText(text, FloatPos.Origin, color)
}

fun rectDrawNode(size: Size, color: Color) = DrawNode {
    drawRect(left = 0, top = 0, right = size.width, bottom = size.height, color = color)
}