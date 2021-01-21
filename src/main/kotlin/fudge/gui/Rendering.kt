package fudge.gui

import NodeObject
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.util.math.MatrixStack

fun interface RenderModifier : Modifier {
    fun RenderContext.render()
}

class RenderContext(val textRenderer: TextRenderer,val matrices: MatrixStack) {

}

fun Modifier.draw(render: RenderContext.() -> Unit) = this wrap RenderModifier(render)

internal fun render(root: NodeObject, renderContext: RenderContext) {
    for (renderer in root.renderCallbacks) {
        with(renderer) {
            renderContext.render()
        }
    }
    for(child in root.children) render(child,renderContext)
}