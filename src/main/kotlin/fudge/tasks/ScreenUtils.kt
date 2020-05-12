package fudge.tasks

import net.minecraft.client.MinecraftClient

object ScreenUtils {
    val width get() = MinecraftClient.getInstance().window.scaledWidth
    val height get() = MinecraftClient.getInstance().window.scaledHeight
}