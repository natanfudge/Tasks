package fudge.gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import fudge.gui.FabricateUi
import fudge.gui.layout.Placed
import fudge.mixinHandlers.HudRenderFunction
import fudge.mixinHandlers.unsubscribe


internal class ActiveGui(
    val composable: @Composable() () -> Unit,
    val composition: Composition,
    val drawCallback: HudRenderFunction
) {
    fun dispose() {
        composition.dispose()
        drawCallback.unsubscribe()
    }
}

internal object FabricateDebug {
    var drawBorders = false
    fun toggleBorders() {
        refreshGui()
        drawBorders = !drawBorders
    }

    fun refreshGui() {
        activeGui!!.dispose()
        FabricateUi.compose(activeGui!!.composable)
    }

    fun init(
        composable: @Composable() () -> Unit,
        composition: Composition,
        drawCallback: HudRenderFunction
    ) {
        activeGui = ActiveGui(composable, composition, drawCallback)
    }

    fun printNode(node: Placed) {
        println(node.debugString())
    }

    private var activeGui: ActiveGui? = null

}


internal fun Placed.debugString(depth: Int = 0): String {
    val x = constraints.x
    val y = constraints.y
    val x2 = constraints.x + constraints.width
    val y2 = constraints.y + constraints.height
    return " ".repeat(depth) + "{${node.debugName}: [$x,$y] -> [$x2,$y2]}" +
            if (children.isNotEmpty()) ": [\n" + children.joinToString(",\n") { it.debugString(depth + 1) } + "\n" + " ".repeat(
                depth
            ) + "]"
            else ""
}