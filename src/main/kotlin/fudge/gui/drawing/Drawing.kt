package fudge.gui.drawing

import fudge.gui.layout.Placed
import fudge.gui.minecraft.MinecraftGui
import net.minecraft.client.MinecraftClient

object Drawing {
    internal fun drawGui(rootPlaced: Placed) = MinecraftGui.draw {
        draw(
            rootPlaced,
            Canvas(it, MinecraftClient.getInstance().textRenderer)
        )
    }

    private fun draw(placeable: Placed, canvas: Canvas) {
        canvas.translate(placeable.constraints.x, placeable.constraints.y)
        val renderContext = DrawContext(canvas, Size(placeable.constraints.width, placeable.constraints.height))
        for (renderer in placeable.node.renderCallbacks) {
            with(renderer) {
                renderContext.render()
            }
        }
        canvas.translate(-placeable.constraints.x, -placeable.constraints.y)
        for (child in placeable.children) draw(child, canvas)
    }
}

