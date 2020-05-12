package fudge.tasks.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack


interface Canvas {
    fun drawRect(left :Int, top : Int, right :Int, bottom: Int, color: Color)
    fun drawText(text: String, pos: FloatPos, color: Color)
    fun translate(top : Int, left : Int)
}

class McCanvas(private val stack: MatrixStack) : Canvas {
    private var x: Int = 0
    private var y: Int = 0

    private val mc = MinecraftClient.getInstance()

    private fun Color.toInt() = red + (green.toInt() shl 8) + (blue.toInt() shl 16) + (alpha.toInt() shl 24)

    override fun drawRect(left :Int, top : Int, right :Int, bottom: Int, color: Color) {
        DrawableHelper.fill(stack, x + left, y + top, x + right, y + bottom, color.toInt())
    }

    override fun drawText(text: String, pos: FloatPos, color: Color) {
        mc.textRenderer.draw(stack, text,x+ pos.left,y+ pos.top, color.toInt())
    }

    override fun translate(top: Int, left: Int) {
        this.x += left
        this.y += top
    }
}

