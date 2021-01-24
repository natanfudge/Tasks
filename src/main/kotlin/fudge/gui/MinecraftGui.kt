package fudge.gui

import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import net.minecraft.client.util.math.MatrixStack

object MinecraftGui {
    fun draw(callback: (MatrixStack) -> Unit) = Events.OnHudRender { matrixStack, _ ->
        callback(matrixStack)
    }
}