package fudge.gui

import androidx.compose.runtime.Composable
import fudge.gui.compose.Node
import fudge.gui.drawing.Color
import fudge.gui.layout.NodeSize
import fudge.gui.layout.Rect
import net.minecraft.client.MinecraftClient

@Composable
fun box(color: Color, modifier: Modifier = Modifier) = Node("Box", modifier.draw {
    canvas.fill(color, Rect(0, 0, size.width, size.height))
}){}

@Composable
fun text(text: String, color: Color, modifier: Modifier = Modifier) {
    Node("Text", modifier.draw {
        canvas.drawText(text, 0, 0, color)
    }.size(MinecraftClient.getInstance().textRenderer.getWidth(text), 8)){}
}

@Composable
fun stack(modifier: Modifier = Modifier, children: @Composable() () -> Unit) =
    Node("Stack", modifier.layout { childrenSizes, constraints ->
        childrenSizes.map { Rect(constraints.x, constraints.y, it.minWidth, it.minHeight)/*.constrain(constraints)*/ }
    }) {
        children()
    }

@Composable
fun column(modifier: Modifier = Modifier, children: @Composable() () -> Unit) =
    Node("Column", modifier.layout { childrenSizes, constraints ->
        val x = constraints.x
        var currentY = constraints.y
        childrenSizes.map { nodeSize ->
            Rect(x, currentY, nodeSize.minWidth, nodeSize.minHeight)
                .also { currentY += nodeSize.minHeight }
        }
    }.size { nodeSizes ->
        val minWidth = nodeSizes.maxOf { it.minWidth }
        val maxWidth = nodeSizes.maxOf { it.maxWidth }
        val minHeight = nodeSizes.sumSafely { minHeight }
        val maxHeight = nodeSizes.sumSafely { maxHeight }
        NodeSize(maxWidth, minWidth, minHeight, maxHeight)
    }){
        children()
    }

private fun List<NodeSize>.sumSafely(property: NodeSize.() -> Int) =
    if (any { it.property() == Int.MAX_VALUE }) Int.MAX_VALUE
    else sumOf { it.property() }


//@Composable
//fun StackElement()