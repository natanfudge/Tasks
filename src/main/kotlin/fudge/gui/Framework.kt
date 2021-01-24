@file:Suppress("FunctionName")

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import fudge.gui.*
import fudge.mixinHandlers.HudRenderFunction
import fudge.mixinHandlers.unsubscribe
import fudge.tasks.ScreenUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient


private val listeners = mutableListOf<(String) -> Unit>()
fun onConsoleInput(listener: (String) -> Unit) {
    // Will only work with exactly one listener, for simplicity
    if (listeners.isEmpty()) listeners.add(listener)
}


internal object GuiCheats {
    var currentComposer: Composer<*>? = null
}

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

internal fun refreshGui() {
    activeGui!!.dispose()
    compose(activeGui!!.composable)
}

private var activeGui: ActiveGui? = null

@OptIn(ExperimentalComposeApi::class)
fun compose(composable: @Composable() () -> Unit) {
    GlobalScope.launch {
        val root = ComposableObject(RootLayout, "<ROOT>")

        Snapshot.registerGlobalWriteObserver { Snapshot.sendApplyNotifications() }

        val rootConstraints = Rectangle(0, 0, ScreenUtils.width, ScreenUtils.height)

        var rootPlacement: Placeable? = null

        val composition = compositionFor(
            root,
            NodeChangeApplier(root) {
                val sizeTree = root.buildSizeTree()
                rootPlacement = Placeable(
                    constraints = rootConstraints,
                    children = root.placeChildren(sizeTree, rootConstraints),
                    node = root
                )

                println(rootPlacement!!.debugString())
            },
            Recomposer.current()
        ) {
            println("Created Composition!")
        }

        composition.setContent {
//        GuiCheats.currentComposer = currentComposer
            composable()
        }

        val drawCallback = MinecraftGui.draw {
            render(
                rootPlacement ?: error("GUI tree was not built!"),
                Canvas(it, MinecraftClient.getInstance().textRenderer)
            )
        }
        //TODO: dipose of composition

        activeGui = ActiveGui(composable, composition, drawCallback)

//    listenToUserInput(composition)
    }

}

private fun Placeable.debugString(depth: Int = 0): String {
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


//private fun CoroutineScope.listenToUserInput(composition: Composition) {

//@Composable
//fun Draw(modifier: Modifier = Modifier, draw: RenderContext.() -> Unit) = Node("Draw", )

@Composable
fun Box(color: Color, modifier: Modifier = Modifier) = Node("Box", modifier.draw {
    canvas.fill(color, Rectangle(0, 0, size.width, size.height))
})

fun Modifier.border(all: Int, color: Color) = border(all, all, all, all, color)

fun Modifier.border(
    left: Int,
    right: Int,
    top: Int,
    bottom: Int,
    color: Color
) = size("Border SizeModifier") {
    assert(it.size == 1) { "Border may only be applied on a singular child" }
    val size = it[0]
    val bonusWidth = left + right
    val bonusHeight = top + bottom
    size + NodeSize(bonusWidth, bonusWidth, bonusHeight, bonusHeight)
}.layout { _, constraints ->
    listOf(
        Rectangle(
            constraints.x + left,
            constraints.y + top,
            constraints.width - left - right,
            constraints.height - top - bottom
        )
    )
}
    .draw {
        // Fill left side (including corners)
        if (left > 0) canvas.fill(color, Rectangle(0, 0, left, size.height))
        // Fill top side (not including corners)
        if (top > 0) canvas.fill(color, Rectangle(left, 0, size.width - left - right, top))
        // Fill right side (including corners)
        if (right > 0) canvas.fill(color, Rectangle(size.width - right, 0, right, size.height))
        // Fill bottom side (not including corners)
        if (bottom > 0) canvas.fill(color, Rectangle(left, size.height - bottom, size.width - left - right, bottom))
    }.wrapNode("Border")

//fun Modifier.border(left: Int, right: Int, top: Int, bottom: Int, color: Color, modifier: Modifier = Modifier) =
//    Node("border", modifier draw {
//
//    })

//TODO: add highlighting for all elements on demand, ASAP

// TODO: somehow make this into a modifier, prob by making a NodeModifier which just spawns a node.
//@Composable
//fun Align(alignment: Alignment, modifier: Modifier = Modifier, children: @Composable() () -> Unit) {
//    Node("Align", modifier = modifier.positionChildren { childrenSizes, constraints ->
//        assert(childrenSizes.size == 1) { "Align node can only have one child!" }
//        val childSize = childrenSizes[0]
//
//        val xManeuverRoom = constraints.width - childSize.minWidth
//        val yManeuverRoom = constraints.height - childSize.minHeight
//        //TODO: this only centers the top left of the element, instead of the element itself. fix that.
//        val x = constraints.x + (xManeuverRoom * alignment.x).toInt()
//        val y = constraints.y + (yManeuverRoom * alignment.y).toInt()
//
//        val childConstraint = Point(x, y)
//        listOf(childConstraint)
//    }) {
//        children()
//    }
//}


class WrapperModifier(val debugName: String, val modifier: Modifier) : Modifier

fun Modifier.wrapNode(debugName: String): Modifier = WrapperModifier(debugName, this)

fun Modifier.align(alignment: Alignment) = positionChildren { childrenSizes, constraints ->
    assert(childrenSizes.size == 1) { "Align node can only have one child!" }
    val childSize = childrenSizes[0]

    val xManeuverRoom = constraints.width - childSize.minWidth
    val yManeuverRoom = constraints.height - childSize.minHeight
    val x = constraints.x + (xManeuverRoom * alignment.x).toInt()
    val y = constraints.y + (yManeuverRoom * alignment.y).toInt()

    val childConstraint = Point(x, y)
    listOf(childConstraint)
}.wrapNode("Align")

///**
// * In case we have something like this:
// * SizeModifier wrap LayoutModifier wrap WrapperModifier wrap SizeModifier wrap WrapperModifier wrap LayoutModifier
// *
// * the SizeModifier and LayoutModifier belong to the first WrapperModifier, the second SizeModifier belong to the second WrapperModifier,
// * and the LayoutModifier at the end belongs to the node itself.
// */
//private data class ModifierParts(
//    val wrapperModifiersModifiers: List<Pair<WrapperModifier, Modifier>>,
//    val nodeItselfModifiers: Modifier
//)
//
///**
// * see [ModifierParts]
// */
//private fun cutModifier(modifier: Modifier): ModifierParts {
//    val wrapperModifiersModifiers = TreeMap<WrapperModifier, MutableList<Modifier>>()
//    val nodeItselfModifiers = mutableListOf<Modifier>()
//    var currentWrapperModifier: WrapperModifier? = null
//    for (innerModifier in modifier.toList().reversed()) {
//        if (innerModifier is WrapperModifier) {
//            wrapperModifiersModifiers[innerModifier] = mutableListOf()
//            currentWrapperModifier = innerModifier
//        } else {
//            if (currentWrapperModifier != null) {
//                wrapperModifiersModifiers.getValue(currentWrapperModifier).add(innerModifier)
//            } else {
//                nodeItselfModifiers.add(innerModifier)
//            }
//        }
//    }
//
//    return ModifierParts(
//        wrapperModifiersModifiers.map { entry -> entry.key to Modifier.fromList(entry.value) },
//        Modifier.fromList(nodeItselfModifiers)
//    )
//}

@Composable
fun Node(
    debugName: String,
    modifier: Modifier = Modifier,
    children: @Composable() () -> Unit = @Composable {},
) {
    val wrapperModifiers = modifier.unwrapWrapperModifiers()
    UnwrapWrapperModifiers(debugName, modifier, children, wrapperModifiers, 0)
}

private fun Modifier.unwrapWrapperModifiers(): List<WrapperModifier> {
    val wrappersFromEndToStart = mutableListOf<WrapperModifier>()
    unwrapWrapperModifiersRecur(wrappersFromEndToStart)
    return wrappersFromEndToStart.reversed()
}

private fun Modifier.unwrapWrapperModifiersRecur(collected: MutableList<WrapperModifier>) {
    val wrapperModifiers = toList().filterIsInstance<WrapperModifier>()
    if (wrapperModifiers.isEmpty()) return
    assert(wrapperModifiers.size == 1) { "Wrapper modifiers don't work by using the normal [wrap] method of other modifiers." }
    val wrapperModifier = wrapperModifiers[0]
    collected.add(wrapperModifier)
    wrapperModifier.modifier.unwrapWrapperModifiersRecur(collected)
}

/**
 * If there are 3 wrapper modifiers for example, it would look like this:
 * NodeInternal(debugName0,modifier0) {
 *   NodeInternal(debugName1,modifier1) {
 *     NodeInternal(debugName2,modifier2) {
 *       NodeInternal(debugName,modifier,children)
 *     }
 *   }
 * }
 */
@Composable
private fun UnwrapWrapperModifiers(
    debugName: String,
    modifier: Modifier,
    children: @Composable() () -> Unit,
    wrapperModifiers: List<WrapperModifier>,
    wrapperModifierIndex: Int,
) {
    if (wrapperModifierIndex < wrapperModifiers.size) {
        val wrapperModifier = wrapperModifiers[wrapperModifierIndex]
        NodeInternal(wrapperModifier.debugName, wrapperModifier.modifier) {
            UnwrapWrapperModifiers(debugName, modifier, children, wrapperModifiers, wrapperModifierIndex + 1)
        }
    } else {
        // Just emit the actual [children]
        NodeInternal(debugName, modifier, children)
    }
}


@Composable
private fun NodeInternal(
    debugName: String,
    modifier: Modifier,
    children: @Composable() () -> Unit,
) {
    emit<ComposableObject, NodeChangeApplier>(
        factory = { ComposableObject(modifier, debugName) },
        update = {
            // Use this to set modifiers, if you decide to make a similar system, i.e.
            set(modifier) { modifier -> this.modifier = modifier }
        },
        content = {
            children()

        }
    )
}



