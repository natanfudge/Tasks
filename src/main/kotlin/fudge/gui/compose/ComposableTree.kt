package fudge.gui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.emit
import fudge.gui.Modifier
import fudge.gui.drawing.Color
import fudge.gui.drawing.renderBorder
import fudge.gui.layout.borderModifier
import fudge.gui.toList

@Composable
fun Node(
    debugName: String,
    modifier: Modifier = Modifier,
    children: @Composable() () -> Unit ,
) {
    val wrapperModifiers = modifier.unwrapWrapperModifiers()
    emitWrapperModifiers(debugName, modifier, children, wrapperModifiers, 0)
}

internal class WrapperModifier(val debugName: String, val modifier: Modifier) : Modifier

fun Modifier.wrapNode(debugName: String): Modifier = WrapperModifier(debugName, this)

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
 * fudge.gui.NodeInternal(debugName0,modifier0) {
 *   fudge.gui.NodeInternal(debugName1,modifier1) {
 *     fudge.gui.NodeInternal(debugName2,modifier2) {
 *       fudge.gui.NodeInternal(debugName,modifier,children)
 *     }
 *   }
 * }
 */
@Composable
private fun emitWrapperModifiers(
    debugName: String,
    modifier: Modifier,
    children: @Composable() () -> Unit,
    wrapperModifiers: List<WrapperModifier>,
    wrapperModifierIndex: Int,
) {
    val actualNode = @Composable {
        if (wrapperModifierIndex < wrapperModifiers.size) {
            val wrapperModifier = wrapperModifiers[wrapperModifierIndex]
            NodeInternal(wrapperModifier.debugName, wrapperModifier.modifier) {
                emitWrapperModifiers(debugName, modifier, children, wrapperModifiers, wrapperModifierIndex + 1)
            }
        } else {
            // Just emit the actual [children]
            NodeInternal(debugName, modifier, children)
        }
    }
    //TODO: a bit more deterministic debug borders
    if (FabricateDebug.drawBorders) {
        val color = Color.random()
        NodeInternal("Debug Border", Modifier.borderModifier(1, 1, 1, 1) {
            renderBorder(1, 1, 1, 1, color)
        }) {
            actualNode()
        }
    } else {
        actualNode()
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


