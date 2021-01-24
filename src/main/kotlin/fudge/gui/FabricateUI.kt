package fudge.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import fudge.gui.compose.ComposableObject
import fudge.gui.compose.Compose
import fudge.gui.compose.FabricateDebug
import fudge.gui.drawing.Drawing
import fudge.gui.layout.Layout
import fudge.gui.layout.Layouts
import fudge.gui.layout.Placed
import fudge.gui.minecraft.MinecraftGui
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * FabricateUI in a nutshell:
 *
 * STEP A: [ComposableObject]
 *
 * First the user describes the GUI in terms of @Composable functions;
 * each Composable creates a [ComposableObject], with the only information attached being [Modifier]s.
 * A modifier supplies 3 key characteristics of a GUI node:
 * - The size of the node [SizeModifier].
 * - How a node layouts its children [LayoutModifier].
 * - What a node draws on the screen [RenderModifier].
 * - TODO: 4th thing - events
 *
 * STEP B: [SizeTree]
 *
 * In order to layout elements on the screen, a parent must first know the size of its children.
 * So, based off of the [SizeModifier], a parent asks its children what its size is. It in turn must ask its children what their size is.
 * All this information is consolidated into the [SizeTree]. We now know the size of each element in the GUI tree.
 *
 * STEP C: [Placed]
 *
 * Now that a parent knows how much space each of its children takes, it lays out its children based off of the [LayoutModifier],
 * after having being laid out itself by its own parent.
 * This creates a tree of [Placed]s, which define exactly where each element is located on the screen.
 *
 * STEP D: [Canvas]
 *
 * Now that every element knows WHERE to draw, it will now describe WHAT to draw, in the [RenderModifier].
 * [RenderModifier]s are provided with the [Canvas], which is the tool for drawing things, as well as the maximum size they are allowed to draw in.
 * Note that [RenderModifier]s should always draw from Reshit Hatzirim ([0,0]), since the actual location they will draw in will be adjusted automatically,
 * using the [Placed] system.
 *
 * Things of note:
 * - A: [WrapperModifier]s.
 * [WrapperModifier]s are a special kind of modifier. Normally, modifiers wrap each other, creating a simple list of modifiers, i.e.:
 * ```
 * modifier1 wrap modifier2 wrap modifier3
 * ```
 * However, [WrapperModifier] come and take "ownership" of all modifiers before them, for example:
 * ```
 * modifier1 wrap modifier2 wrap wrapperModifier1 wrap modifier3 wrap wrapperModifier2 wrap modifier4
 * ```
 *
 * In this case, `wrapperModifier1` **owns** `modifier1` and `modifier2`, `wrapperModifier2` **owns** `modifier3`,
 * and `modifier4` belongs to the [ComposableObject] we applied this modifier to in the first place.
 * Then, after ownership is resolved, each [WrapperModifier] spawns a [ComposableObject], using its owned modifiers,
 * with the node we applied this to in the first place, getting `modifier4`. It will spawn something like this:
 * ```
 * NodeForWrapper1(modifier1 wrap modifier2) {
 *   NodeForWrapper2(modifier3) {
 *      OriginalNode(modifier4)
 *   }
 * }
 * ```
 *
 * The BENEFIT of this is that we can replace GUI elements such as Padding {} with modifiers such as .padding(), which avoids nesting
 * and solves the problem of multiple children.
 *
 * //TODO: step E: events
 */

object FabricateUi {
    @OptIn(ExperimentalComposeApi::class)
    fun compose(composable: @Composable() () -> Unit) {
        GlobalScope.launch {
            Compose.init()

            val root = ComposableObject(Layouts.Root, "<ROOT>")

            var rootPlacement: Placed? = null

            val composition = Compose.buildComposition(composable, root ,onChange = {
                rootPlacement = Layout.layoutGuiTree(root, MinecraftGui.screenConstraints)
                FabricateDebug.printNode(rootPlacement!!)
            })

            val drawCallback = Drawing.drawGui(rootPlacement  ?: error("GUI tree was not built!"))
            //TODO: dipose of composition
            FabricateDebug.init(composable, composition, drawCallback)
        }

    }

}


