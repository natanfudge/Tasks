package fudge.gui

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack


data class Color(val alpha: Int, val red: Int, val green: Int, val blue: Int) {
    init {
        require(alpha in 0..255)
        require(red in 0..255)
        require(green in 0..255)
        require(blue in 0..255)
    }

    internal fun toMcFormat(): Int = (alpha shl 24) + (red shl 16) + (green shl 8) + blue

    companion object {
        val Black = Color(255, 0, 0, 0)
        val Red = Color(255,255,0,0)
        val Green = Color(255,0,255,0)
        val Blue = Color(255,0,0,255)
    }
}

//TODO: make matrices private instead of internal
class Canvas(internal val matrices: MatrixStack, private val textRenderer: TextRenderer) {
    private var currentX = 0
    private var currentY = 0

    fun drawText(text: String, x: Int, y: Int, color: Color) {
        textRenderer.draw(matrices, text, currentX + x.toFloat(), currentY + y.toFloat(), color.toMcFormat())
    }

    fun fill(color: Color, area: Rectangle) {
        val x = currentX + area.x
        val y = currentY + area.y
        DrawableHelper.fill(matrices, x, y, x + area.width, y + area.height, color.toMcFormat())
    }

    internal fun translate(x: Int, y: Int) {
        currentX += x
        currentY += y
    }
}
