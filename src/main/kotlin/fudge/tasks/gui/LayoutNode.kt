package fudge.tasks.gui

import org.lwjgl.system.CallbackI
import kotlin.math.min


interface LayoutNode {
    val minSize: Size
    val maxSize: Size

    //    val preferredSize: Size
    fun layout(bounds: Rect): Element
}


class ColumnLayoutNode(private val children: List<LayoutNode>) : LayoutNode {
    init {
        require(children.isNotEmpty())
    }

    private inline fun <T> List<T>.fit(sizeGetter: (T) -> Size) = Size(
        height = sumBy { sizeGetter(it).height },
        width = sizeGetter(maxBy { sizeGetter(it).width }!!).width
    )

    override val minSize = children.fit { it.minSize }
    override val maxSize = children.fit { it.maxSize }
//    override val preferredSize = children.fit { it.preferredSize }

    override fun layout(bounds: Rect): Element {
        var currentHeight = 0
        var availableHeight = bounds.bottom - bounds.top
        val availableWidth = bounds.right - bounds.left
        val result = children.map {
            val heightUsed = min(availableHeight, it.minSize.height)
            val layout = it.layout(
                Rect(
                    left = 0,
                    top = currentHeight,
                    bottom = currentHeight + heightUsed,
                    right = min(availableWidth, it.minSize.width)
                )
            )

            availableHeight -= heightUsed
            currentHeight += heightUsed

            layout
        }
        return ChildrenElement(
            bounds = Rect.fromLTWH(
                left = bounds.left, top = bounds.top, height = currentHeight, width = min(availableWidth, minSize.width)
            ),
            children = result
        )
    }

}


abstract class Element(val bounds: Rect) {
    abstract fun draw(canvas: Canvas)

    protected inline fun Canvas.withTranslation(bounds: Rect, drawer: () -> Unit) {
        translate(bounds.left, bounds.top)
        drawer()
        translate(-bounds.left, -bounds.top)
    }
}


class ChildrenElement(bounds: Rect,  val children: List<Element>) : Element(bounds) {
    override fun draw(canvas: Canvas) {
        canvas.withTranslation(bounds) {
            children.forEach {
                it.draw(canvas)
            }
        }
    }
}

class DrawElement(bounds: Rect, private val drawNode: DrawNode) : Element(bounds) {
    constructor(bounds: Rect, drawer: DrawContext.() -> Unit) : this(bounds, DrawNode(drawer))
    override fun draw(canvas: Canvas) {
        with(drawNode) {
            canvas.withTranslation(bounds) {
                DrawContextImpl(bounds.getSize(), canvas).draw()
            }
        }
    }
}

