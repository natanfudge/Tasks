package fudge.tasks.gui

interface Component {
    fun getLayout(): LayoutNode

    companion object {
        inline operator fun invoke(crossinline layout: () -> LayoutNode) = object : Component {
            override fun getLayout(): LayoutNode = layout()
        }
    }
}



fun column(children: List<Component>) = Component {
    ColumnLayoutNode(children.map { it.getLayout() })
}

fun box(width: Int, height: Int, color: Color) = Component {
    object : LayoutNode {
        override val minSize = Size(width = width, height = height)
        override val maxSize = Size.Infinity

        override fun layout(bounds: Rect): Element = DrawElement(bounds) {
            drawRect(left = 0, top = 0, right = size.width, bottom = size.height, color = color)
        }

    }
}