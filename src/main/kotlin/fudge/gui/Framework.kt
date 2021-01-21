import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import fudge.gui.Modifier
import fudge.gui.RenderContext
import fudge.gui.draw
import fudge.gui.render
import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient


private val listeners = mutableListOf<(String) -> Unit>()
fun onConsoleInput(listener: (String) -> Unit) {
    // Will only work with exactly one listener, for simplicity
    if (listeners.isEmpty()) listeners.add(listener)
}

@OptIn(ExperimentalComposeApi::class)
suspend fun compose(
    composable: @Composable() () -> Unit
): Unit = coroutineScope {
    val root = NodeObject( Modifier)

    Snapshot.registerGlobalWriteObserver { Snapshot.sendApplyNotifications() }

    val composition = compositionFor(
        root,
        NodeChangeApplier(root) {
//            displayNewTree(root)
        },
        Recomposer.current()
    ) {
        println("Created Composition!")
    }

    composition.setContent {
        composable()
    }

    Events.OnHudRender(
        HudRenderCallback { stack, delta ->
            val renderContext = RenderContext(MinecraftClient.getInstance().textRenderer,stack)
            render(root, renderContext)
        }
    )

    listenToUserInput(composition)
}



private fun CoroutineScope.listenToUserInput(composition: Composition) {
//    launch {
//        while (true) {
//            val input = Scanner(System.`in`).next()
//            if (input == "STOP") {
//                composition.dispose()
//                break
//            }
//            for (listener in listeners) {
//                listener(input)
//            }
//        }
//    }
}

//private fun displayNewTree(root: Node) {
//    println(root.toConsoleTree())
//}

@Composable
fun Draw(modifier: Modifier = Modifier, draw: RenderContext.() -> Unit)  = Node(modifier.draw(draw))

@Composable
fun Node(
    modifier: Modifier = Modifier,
    children: @Composable() () -> Unit = @Composable {},
) {

    emit<NodeObject, NodeChangeApplier>(
        factory = { NodeObject(modifier) },
        update = {
            // Use this to set modifiers, if you decide to make a similar system, i.e.
            // set(modifier) { modifier -> this.modifier = modifier }
        },
        content = {
                children()

        }
    )
}



