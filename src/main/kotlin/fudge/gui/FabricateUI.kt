package fudge.gui

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
 * STEP C: [Placeable]
 *
 * Now that a parent knows how much space each of its children takes, it lays out its children based off of the [LayoutModifier],
 * after having being laid out itself by its own parent.
 * This creates a tree of [Placeable]s, which define exactly where each element is located on the screen.
 *
 * STEP D: [Canvas]
 *
 * Now that every element knows WHERE to draw, it will now describe WHAT to draw, in the [RenderModifier].
 * [RenderModifier]s are provided with the [Canvas], which is the tool for drawing things, as well as the maximum size they are allowed to draw in.
 * Note that [RenderModifier]s should always draw from Reshit Hatzirim ([0,0]), since the actual location they will draw in will be adjusted automatically,
 * using the [Placeable] system.
 *
 * //TODO: step E: events
 */

class Foo {

}