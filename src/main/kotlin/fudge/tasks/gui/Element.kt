package fudge.tasks.gui

import fudge.tasks.gui.compose.AlignmentLine

abstract class Element(val size: Size) {
    abstract fun draw(canvas: Canvas)

    //TODO: instead of this mutable garbage use a seperate PlacedElement widget?
    private var position: IntPos? = null

    fun place(pos: IntPos) {
        position = pos
    }


    internal inline fun Canvas.withTranslation(drawer: () -> Unit) {
        val pos = position!!
        translate(pos.left, pos.top)
        drawer()
        translate(-pos.left, -pos.top)
    }

    //TODO
    operator fun get(line: AlignmentLine): Int? = null
//             = with(modifier) {
//        return layoutNode.measureScope.modifyAlignmentLine(
//            line,
//            super.get(line),
//            measureScope.layoutDirection
//        )
//    }
}


class ChildrenElement(size: Size, val children: List<Element>) : Element(size) {
    override fun draw(canvas: Canvas) {
        canvas.withTranslation {
            children.forEach {
                it.draw(canvas)
            }
        }
    }
}

class DrawElement(size: Size, private val drawNode: DrawNode) : Element(size) {
    constructor(size: Size, drawer: DrawContext.() -> Unit) : this(size, DrawNode(drawer))

    override fun draw(canvas: Canvas) {
        with(drawNode) {
            canvas.withTranslation {
                DrawContextImpl(size, canvas).draw()
            }
        }
    }
}

