package fudge.tasks.gui

import fudge.tasks.ScreenUtils


class ComponentManager(private val component: Component) {
    var root = createRoot()

    private fun createRoot(): Element {
        val result = component.getLayout()
            .layout(Rect(left = 0,top = 0, right = ScreenUtils.width, bottom = ScreenUtils.height))

        println("ROOT ELEMENT: " + result.toReadableString())

        return result
    }

    fun rebuild() {
        root = createRoot()
    }

    fun draw(canvas: Canvas) = root.draw(canvas)
}


fun Element.toReadableString(indent: Int = 0): String = when (this) {
    is ChildrenElement -> "\t".repeat(indent) + "- [" + "\n" + children.joinToString(",\n") { it.toReadableString(indent + 1) } + "\n" + "]"
    else -> "\t".repeat(indent) + "- (${bounds.left}, ${bounds.top}) -> (${bounds.right}, ${bounds.bottom})"
}