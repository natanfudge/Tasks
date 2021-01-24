package fudge.gui

fun interface RenderModifier : Modifier {
    fun RenderContext.render()
}

data class Size(val width: Int, val height: Int)
class RenderContext(val canvas: Canvas, val size: Size)

fun Modifier.draw(render: RenderContext.() -> Unit) = this wrap RenderModifier(render)


internal fun render(placeable: Placeable, canvas: Canvas) {
    canvas.translate(placeable.constraints.x, placeable.constraints.y)
    val renderContext = RenderContext(canvas, Size(placeable.constraints.width, placeable.constraints.height))
    for (renderer in placeable.node.renderCallbacks) {
        with(renderer) {
            renderContext.render()
        }
    }
    canvas.translate(-placeable.constraints.x, -placeable.constraints.y)
    for (child in placeable.children) render(child, canvas)
}