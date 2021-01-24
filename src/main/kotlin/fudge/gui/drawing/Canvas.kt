package fudge.gui.drawing

import fudge.gui.layout.Rect
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack

data class Size(val width: Int, val height: Int)
class DrawContext(val canvas: Canvas, val size: Size)


class Canvas(private val matrices: MatrixStack, private val textRenderer: TextRenderer) {
    private var currentX = 0
    private var currentY = 0

    fun drawText(text: String, x: Int, y: Int, color: Color) {
        textRenderer.draw(matrices, text, currentX + x.toFloat(), currentY + y.toFloat(), color.toMcFormat())
    }

    fun fill(color: Color, area: Rect) {
        val x = currentX + area.x
        val y = currentY + area.y
        DrawableHelper.fill(matrices, x, y, x + area.width, y + area.height, color.toMcFormat())
    }

    internal fun translate(x: Int, y: Int) {
        currentX += x
        currentY += y
    }
}
