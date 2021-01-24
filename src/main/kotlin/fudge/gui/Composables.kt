package fudge.gui

import androidx.compose.runtime.Composable
import fudge.gui.*
import fudge.gui.compose.Node
import fudge.gui.drawing.Color
import fudge.gui.layout.Rect
import net.minecraft.client.MinecraftClient

@Composable
fun BoxElement(color: Color, modifier: Modifier = Modifier) = Node("Box", modifier.draw {
    canvas.fill(color, Rect(0, 0, size.width, size.height))
})

@Composable
fun TextElement(text: String, color: Color, modifier: Modifier = Modifier) {
    Node("text", modifier.draw {
        canvas.drawText(text, 0, 0, color)
    }.size(MinecraftClient.getInstance().textRenderer.getWidth(text), 8))
}