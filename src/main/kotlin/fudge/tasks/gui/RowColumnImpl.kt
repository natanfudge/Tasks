@file:Suppress("NOTHING_TO_INLINE")

package fudge.tasks.gui

import fudge.tasks.gui.LayoutOrientation.Horizontal
import fudge.tasks.gui.LayoutOrientation.Vertical
import fudge.tasks.gui.compose.*
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sign

/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


internal class ColumnLayoutNode(private val orientation: LayoutOrientation,
                       private val arrangement: Arrangement,
                       private val crossAxisSize: SizeMode,private val children: List<LayoutNode>) : LayoutNode {
    init {
        require(children.isNotEmpty())
    }

    private inline fun <T> List<T>.fit(sizeGetter: (T) -> Size) = Size(
        height = sumBy { sizeGetter(it).height },
        width = sizeGetter(maxBy { sizeGetter(it).width }!!).width
    )

    override val minSize = children.fit { it.minSize }
    override val maxSize = children.fit { it.maxSize }
//    override val preferredSize = children.fit { it.preferredSize }

    override fun layout(bounds: Rect, layoutDirection: LayoutDirection): Element  = rowColumnImpl(

    )

//    }
//        TODO()
//        var currentHeight = 0
//        var availableHeight = bounds.bottom - bounds.top
//        val availableWidth = bounds.right - bounds.left
//        val result = children.map {
//            val heightUsed = min(availableHeight, it.minSize.height)
//            val layout = it.layout(
//                Rect(
//                    left = 0,
//                    top = currentHeight,
//                    bottom = currentHeight + heightUsed,
//                    right = min(availableWidth, it.minSize.width)
//                ),
//                layoutDirection
//            )
//
//            availableHeight -= heightUsed
//            currentHeight += heightUsed
//
//            layout
//        }
//        return ChildrenElement(
//            size =
////            bounds = Rect.fromLTWH(
////                left = bounds.left, top = bounds.top, height = currentHeight, width = min(availableWidth, minSize.width)
////            ),
//            children = result
//        )
    }

}


/**
 * Shared implementation for [Row] and [Column].
 */
internal fun rowColumnImpl(
    orientation: LayoutOrientation,
    arrangement: Arrangement,
    crossAxisSize: SizeMode,
    bounds: Rect,
    layoutDirection: LayoutDirection,
    crossAxisAlignment: Any,
    children: List<LayoutNode>
)  : Element{
    fun Element.mainAxisSize() =
        if (orientation == LayoutOrientation.Horizontal) size.width else size.height

    fun Element.crossAxisSize() =
        if (orientation == LayoutOrientation.Horizontal) size.height else size.width

    fun LayoutNode.alignmentLineProvider() =
        (this.crossAxisAlignment as? CrossAxisAlignment)
            ?.alignmentLineProvider
            ?: (crossAxisAlignment as? CrossAxisAlignment)?.alignmentLineProvider

//    Layout(
//        children,
//        modifier = modifier,
//        minIntrinsicWidthMeasureBlock = MinIntrinsicWidthMeasureBlock(orientation),
//        minIntrinsicHeightMeasureBlock = MinIntrinsicHeightMeasureBlock(orientation),
//        maxIntrinsicWidthMeasureBlock = MaxIntrinsicWidthMeasureBlock(orientation),
//        maxIntrinsicHeightMeasureBlock = MaxIntrinsicHeightMeasureBlock(orientation)
//    ) { ltrMeasurables, outerConstraints, layoutDirection ->
    // rtl support
    val measurables = if (orientation == LayoutOrientation.Horizontal &&
        layoutDirection == LayoutDirection.Rtl
    ) {
        children.asReversed()
    } else {
        children
    }

    val constraints = OrientationIndependentConstraints(bounds, orientation)

    var totalWeight = 0f
    var fixedSpace = 0
    var crossAxisSpace = 0

    var anyAlignWithSiblings = false

    val placeables = arrayOfNulls<Element>(measurables.size)
    // First measure children with zero weight.
    for (i in 0 until measurables.size) {
        val child = measurables[i]
        val weight = child.weight

        if (weight > 0f) {
            totalWeight += child.weight
        } else {
            val placeable = child.layout(
                // Ask for preferred main axis size.
                constraints.copy(
                    mainAxisMin = 0,
                    mainAxisMax = constraints.mainAxisMax - fixedSpace,
                    crossAxisMin = 0
                ).toBoxConstraints(orientation),
                layoutDirection
            )
            fixedSpace += placeable.mainAxisSize()
            crossAxisSpace = max(crossAxisSpace, placeable.crossAxisSize())
            anyAlignWithSiblings = anyAlignWithSiblings || child.alignmentLineProvider() != null
            placeables[i] = placeable
        }
    }

    // Then measure the rest according to their weights in the remaining main axis space.
    val targetSpace = if (totalWeight > 0f && constraints.mainAxisMax.isFinite()) {
        constraints.mainAxisMax
    } else {
        constraints.mainAxisMin
    }

    val weightUnitSpace = if (totalWeight > 0) {
        (targetSpace - fixedSpace) / totalWeight
    } else {
        0f
    }

    var remainder = targetSpace - fixedSpace - measurables.sumBy {
        (weightUnitSpace * it.weight).roundToInt()
    }

    var weightedSpace = 0

    for (i in 0 until measurables.size) {
        val child = measurables[i]
        val weight = child.weight
        if (weight > 0f) {
            val remainderUnit = remainder.sign
            remainder -= remainderUnit
            val childMainAxisSize = max(
                0,
                (weightUnitSpace * child.weight).roundToInt() + remainderUnit
            )
            val placeable = child.layout(
                OrientationIndependentConstraints(
                    if (child.fill && childMainAxisSize.isFinite()) {
                        childMainAxisSize
                    } else {
                        0
                    },
                    childMainAxisSize,
                    0,
                    constraints.crossAxisMax
                ).toBoxConstraints(orientation),
                layoutDirection
            )
            weightedSpace += placeable.mainAxisSize()
            crossAxisSpace = max(crossAxisSpace, placeable.crossAxisSize())
            anyAlignWithSiblings = anyAlignWithSiblings || child.alignmentLineProvider() != null
            placeables[i] = placeable
        }
    }

    var beforeCrossAxisAlignmentLine = 0
    var afterCrossAxisAlignmentLine = 0
    if (anyAlignWithSiblings) {
        for (i in 0 until placeables.size) {
            val placeable = placeables[i]!!
            val lineProvider = measurables[i].alignmentLineProvider()
            if (lineProvider != null) {
                val alignmentLinePosition = when (lineProvider) {
                    is AlignmentLineProvider.Block ->
                        lineProvider.lineProviderBlock(Measured(placeable))
                    is AlignmentLineProvider.Value -> placeable[lineProvider.line]
                }
                beforeCrossAxisAlignmentLine = max(
                    beforeCrossAxisAlignmentLine,
                    alignmentLinePosition ?: 0
                )
                afterCrossAxisAlignmentLine = max(
                    afterCrossAxisAlignmentLine,
                    placeable.crossAxisSize() -
                            (alignmentLinePosition ?: placeable.crossAxisSize())
                )
            }
        }
    }

    // Compute the Row or Column size and position the children.
    val mainAxisLayoutSize = if (totalWeight > 0f && constraints.mainAxisMax.isFinite()) {
        constraints.mainAxisMax
    } else {
        max(fixedSpace + weightedSpace, constraints.mainAxisMin)
    }
    val crossAxisLayoutSize = if (constraints.crossAxisMax.isFinite() &&
        crossAxisSize == SizeMode.Expand
    ) {
        constraints.crossAxisMax
    } else {
        max(
            crossAxisSpace,
            max(
                constraints.crossAxisMin,
                beforeCrossAxisAlignmentLine + afterCrossAxisAlignmentLine
            )
        )
    }
    val layoutWidth = if (orientation == LayoutOrientation.Horizontal) {
        mainAxisLayoutSize
    } else {
        crossAxisLayoutSize
    }
    val layoutHeight = if (orientation == LayoutOrientation.Horizontal) {
        crossAxisLayoutSize
    } else {
        mainAxisLayoutSize
    }
//        layout(layoutWidth, layoutHeight)

    val childrenMainAxisSize = placeables.map { it!!.mainAxisSize() }
    val mainAxisPositions = arrangement.arrange(
        mainAxisLayoutSize,
        childrenMainAxisSize,
        layoutDirection
    )
    placeables.forEachIndexed { index, placeable ->
        placeable!!
        val measurable = measurables[index]
        val childCrossAlignment = measurable.crossAxisAlignment ?: crossAxisAlignment
        val isRtlColumn = orientation == LayoutOrientation.Vertical &&
                layoutDirection == LayoutDirection.Rtl

        val crossAxis = when (childCrossAlignment) {
            CrossAxisAlignment.Start -> {
                if (isRtlColumn) {
                    crossAxisLayoutSize - placeable.crossAxisSize()
                } else {
                    0
                }
            }
            CrossAxisAlignment.End -> {
                if (isRtlColumn) {
                    0
                } else {
                    crossAxisLayoutSize - placeable.crossAxisSize()
                }
            }
            CrossAxisAlignment.Center -> {
                Alignment.Center.align(
                    Size(
                        mainAxisLayoutSize - placeable.mainAxisSize(),
                        crossAxisLayoutSize - placeable.crossAxisSize()
                    )
                ).top
            }
            is Alignment.Vertical -> childCrossAlignment.align(
                crossAxisLayoutSize - placeable.crossAxisSize()
            )
            is Alignment.Horizontal -> childCrossAlignment.align(
                crossAxisLayoutSize - placeable.crossAxisSize(),
                layoutDirection
            )
            is CrossAxisAlignment -> {
                val provider = childCrossAlignment.alignmentLineProvider
                val alignmentLinePosition = when (provider) {
                    is AlignmentLineProvider.Block ->
                        provider.lineProviderBlock(Measured(placeable))
                    is AlignmentLineProvider.Value -> placeable[provider.line]
                    else -> null
                }
                if (alignmentLinePosition != null) {
                    val line = beforeCrossAxisAlignmentLine - alignmentLinePosition
                    if (orientation == LayoutOrientation.Vertical &&
                        layoutDirection == LayoutDirection.Rtl
                    ) {
                        layoutWidth - line - placeable.size.width
                    } else {
                        line
                    }
                } else {
                    0
                }
            }
            else -> 0
        }

        if (orientation == LayoutOrientation.Horizontal) {
            placeable.place(IntPos(mainAxisPositions[index], crossAxis))
        } else {
            placeable.place(IntPos(crossAxis, mainAxisPositions[index]))
        }
    }
    return ChildrenElement(size = Size(height = layoutHeight, width = layoutWidth), children =  placeables.toList().requireNoNulls())
}
//}
//}
internal inline fun Int.isFinite() = this != Int.MAX_VALUE

/**
 * Used to specify the arrangement of the layout's children in [Row] or [Column] in the main axis
 * direction (horizontal and vertical, respectively).
 * @constructor Creates an arrangement using the [arrange] function. Use it to provide your own
 * arrangement of the layout's children.
 */
interface Arrangement {
    /**
     * Places the layout children inside the parent layout along the main axis.
     *
     * @param totalSize Available space that can be occupied by the children.
     * @param size A list of sizes of all children.
     * @param layoutDirection A layout direction, left-to-right or right-to-left, of the parent
     * layout that should be taken into account when determining positions of the children in
     * horizontal direction.
     */
    fun arrange(totalSize: IntPx, size: List<IntPx>, layoutDirection: LayoutDirection): List<IntPx>

    /**
     * Used to specify the vertical arrangement of the layout's children in a [Column].
     */
    interface Vertical : Arrangement

    /**
     * Used to specify the horizontal arrangement of the layout's children in a [Row].
     */
    interface Horizontal : Arrangement

    /**
     * Place children horizontally such that they are as close as possible to the beginning of the
     * main axis.
     */
    object Start : Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ) = if (layoutDirection == LayoutDirection.Ltr) {
            placeLeftOrTop(size)
        } else {
            placeRightOrBottom(totalSize, size)
        }
    }

    /**
     * Place children horizontally such that they are as close as possible to the end of the main
     * axis.
     */
    object End : Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ) = if (layoutDirection == LayoutDirection.Ltr) {
            placeRightOrBottom(totalSize, size)
        } else {
            placeLeftOrTop(size)
        }
    }

    /**
     * Place children vertically such that they are as close as possible to the top of the main
     * axis.
     */
    object Top : Vertical {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ) = placeLeftOrTop(size)
    }

    /**
     * Place children vertically such that they are as close as possible to the bottom of the main
     * axis.
     */
    object Bottom : Vertical {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ) = placeRightOrBottom(totalSize, size)
    }

    /**
     * Place children such that they are as close as possible to the middle of the main axis.
     */
    object Center : Vertical, Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ): List<IntPx> {
            val consumedSize = size.fold(0.ipx) { a, b -> a + b }
            val positions = mutableListOf<IntPx>()
            var current = (totalSize - consumedSize).toPx() / 2
            size.forEach {
                positions.add(current.round())
                current += it
            }
            return positions
        }
    }

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child.
     */
    object SpaceEvenly : Vertical, Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ): List<IntPx> {
            val consumedSize = size.fold(0.ipx) { a, b -> a + b }
            val gapSize = (totalSize - consumedSize).toPx() / (size.size + 1)
            val positions = mutableListOf<IntPx>()
            var current = gapSize
            size.forEach {
                positions.add(current.round())
                current += it.toPx() + gapSize
            }
            return positions
        }
    }

    /**
     * Place children such that they are spaced evenly across the main axis, without free
     * space before the first child or after the last child.
     */
    object SpaceBetween : Vertical, Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ): List<IntPx> {
            val consumedSize = size.fold(0.ipx) { a, b -> a + b }
            val gapSize = if (size.size > 1) {
                (totalSize - consumedSize).toPx() / (size.size - 1)
            } else {
                0.px
            }
            val positions = mutableListOf<IntPx>()
            var current = 0.px
            size.forEach {
                positions.add(current.round())
                current += it.toPx() + gapSize
            }
            return positions
        }
    }

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child, but half the amount of space
     * existing otherwise between two consecutive children.
     */
    object SpaceAround : Vertical, Horizontal {
        override fun arrange(
            totalSize: IntPx,
            size: List<IntPx>,
            layoutDirection: LayoutDirection
        ): List<IntPx> {
            val consumedSize = size.fold(0.ipx) { a, b -> a + b }
            val gapSize = if (size.isNotEmpty()) {
                (totalSize - consumedSize).toPx() / size.size
            } else {
                0.px
            }
            val positions = mutableListOf<IntPx>()
            var current = gapSize / 2
            size.forEach {
                positions.add(current.round())
                current += it.toPx() + gapSize
            }
            return positions
        }
    }

    private companion object {
        private fun placeRightOrBottom(totalSize: IntPx, size: List<IntPx>): List<IntPx> {
            val consumedSize = size.fold(0.ipx) { a, b -> a + b }
            val positions = mutableListOf<IntPx>()
            var current = totalSize - consumedSize
            size.forEach {
                positions.add(current)
                current += it
            }
            return positions
        }

        private fun placeLeftOrTop(size: List<IntPx>): List<IntPx> {
            val positions = mutableListOf<IntPx>()
            var current = 0.ipx
            size.forEach {
                positions.add(current)
                current += it
            }
            return positions
        }
    }
}

/**
 * [Row] will be [Horizontal], [Column] is [Vertical].
 */
internal enum class LayoutOrientation {
    Horizontal,
    Vertical
}

/**
 * Used to specify how a layout chooses its own size when multiple behaviors are possible.
 */
// TODO(popam): remove this when Flow is reworked
enum class SizeMode {
    /**
     * Minimize the amount of free space by wrapping the children,
     * subject to the incoming layout constraints.
     */
    Wrap,

    /**
     * Maximize the amount of free space by expanding to fill the available space,
     * subject to the incoming layout constraints.
     */
    Expand
}

/**
 * Used to specify the alignment of a layout's children, in main axis direction.
 */
enum class MainAxisAlignment(internal val arrangement: Arrangement) {
    // TODO(soboleva) support RTl in Flow
    // workaround for now - use Arrangement that equals to previous Arrangement
    /**
     * Place children such that they are as close as possible to the middle of the main axis.
     */
    Center(Arrangement.Center),

    /**
     * Place children such that they are as close as possible to the start of the main axis.
     */
    Start(Arrangement.Top),

    /**
     * Place children such that they are as close as possible to the end of the main axis.
     */
    End(Arrangement.Bottom),

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child.
     */
    SpaceEvenly(Arrangement.SpaceEvenly),

    /**
     * Place children such that they are spaced evenly across the main axis, without free
     * space before the first child or after the last child.
     */
    SpaceBetween(Arrangement.SpaceBetween),

    /**
     * Place children such that they are spaced evenly across the main axis, including free
     * space before the first child and after the last child, but half the amount of space
     * existing otherwise between two consecutive children.
     */
    SpaceAround(Arrangement.SpaceAround);
}

/**
 * Used to specify the alignment of a layout's children, in cross axis direction.
 */
// TODO(popam): refine this API surface with modifiers - add type safety for alignment orientation.
class CrossAxisAlignment private constructor(
    internal val alignmentLineProvider: AlignmentLineProvider? = null
) {
    companion object {
        /**
         * Place children such that their center is in the middle of the cross axis.
         */
        val Center = CrossAxisAlignment(null)

        /**
         * Place children such that their start edge is aligned to the start edge of the cross
         * axis. TODO(popam): Consider rtl directionality.
         */
        val Start = CrossAxisAlignment(null)

        /**
         * Place children such that their end edge is aligned to the end edge of the cross
         * axis. TODO(popam): Consider rtl directionality.
         */
        val End = CrossAxisAlignment(null)

        /**
         * Align children by their baseline.
         */
        fun AlignmentLine(alignmentLine: AlignmentLine) =
            CrossAxisAlignment(AlignmentLineProvider.Value(alignmentLine))

        /**
         * Align children relative to their siblings using the alignment line provided as a
         * parameter using [AlignmentLineProvider].
         */
        internal fun Relative(alignmentLineProvider: AlignmentLineProvider) =
            CrossAxisAlignment(alignmentLineProvider)
    }
}


private val IntrinsicMeasurable.weight: Float
    get() = (parentData as? RowColumnParentData)?.weight ?: 0f

private val IntrinsicMeasurable.fill: Boolean
    get() = (parentData as? RowColumnParentData)?.fill ?: true

private val IntrinsicMeasurable.crossAxisAlignment: Any?
    get() = (parentData as? RowColumnParentData)?.crossAxisAlignment

private /*inline*/ fun MinIntrinsicWidthMeasureBlock(orientation: LayoutOrientation) =
    if (orientation == LayoutOrientation.Horizontal) {
        IntrinsicMeasureBlocks.HorizontalMinWidth
    } else {
        IntrinsicMeasureBlocks.VerticalMinWidth
    }

private /*inline*/ fun MinIntrinsicHeightMeasureBlock(orientation: LayoutOrientation) =
    if (orientation == LayoutOrientation.Horizontal) {
        IntrinsicMeasureBlocks.HorizontalMinHeight
    } else {
        IntrinsicMeasureBlocks.VerticalMinHeight
    }

private /*inline*/ fun MaxIntrinsicWidthMeasureBlock(orientation: LayoutOrientation) =
    if (orientation == LayoutOrientation.Horizontal) {
        IntrinsicMeasureBlocks.HorizontalMaxWidth
    } else {
        IntrinsicMeasureBlocks.VerticalMaxWidth
    }

private /*inline*/ fun MaxIntrinsicHeightMeasureBlock(orientation: LayoutOrientation) =
    if (orientation == LayoutOrientation.Horizontal) {
        IntrinsicMeasureBlocks.HorizontalMaxHeight
    } else {
        IntrinsicMeasureBlocks.VerticalMaxHeight
    }

private object IntrinsicMeasureBlocks {
    val HorizontalMinWidth: IntrinsicMeasureBlock = { measurables, availableHeight, _ ->
        intrinsicSize(
            measurables,
            { h -> minIntrinsicWidth(h) },
            { w -> maxIntrinsicHeight(w) },
            availableHeight,
            LayoutOrientation.Horizontal,
            LayoutOrientation.Horizontal
        )
    }
    val VerticalMinWidth: IntrinsicMeasureBlock = { measurables, availableHeight, _ ->
        intrinsicSize(
            measurables,
            { h -> minIntrinsicWidth(h) },
            { w -> maxIntrinsicHeight(w) },
            availableHeight,
            LayoutOrientation.Vertical,
            LayoutOrientation.Horizontal
        )
    }
    val HorizontalMinHeight: IntrinsicMeasureBlock = { measurables, availableWidth, _ ->
        intrinsicSize(
            measurables,
            { w -> minIntrinsicHeight(w) },
            { h -> maxIntrinsicWidth(h) },
            availableWidth,
            LayoutOrientation.Horizontal,
            LayoutOrientation.Vertical
        )
    }
    val VerticalMinHeight: IntrinsicMeasureBlock = { measurables, availableWidth, _ ->
        intrinsicSize(
            measurables,
            { w -> minIntrinsicHeight(w) },
            { h -> maxIntrinsicWidth(h) },
            availableWidth,
            LayoutOrientation.Vertical,
            LayoutOrientation.Vertical
        )
    }
    val HorizontalMaxWidth: IntrinsicMeasureBlock = { measurables, availableHeight, _ ->
        intrinsicSize(
            measurables,
            { h -> maxIntrinsicWidth(h) },
            { w -> maxIntrinsicHeight(w) },
            availableHeight,
            LayoutOrientation.Horizontal,
            LayoutOrientation.Horizontal
        )
    }
    val VerticalMaxWidth: IntrinsicMeasureBlock = { measurables, availableHeight, _ ->
        intrinsicSize(
            measurables,
            { h -> maxIntrinsicWidth(h) },
            { w -> maxIntrinsicHeight(w) },
            availableHeight,
            LayoutOrientation.Vertical,
            LayoutOrientation.Horizontal
        )
    }
    val HorizontalMaxHeight: IntrinsicMeasureBlock = { measurables, availableWidth, _ ->
        intrinsicSize(
            measurables,
            { w -> maxIntrinsicHeight(w) },
            { h -> maxIntrinsicWidth(h) },
            availableWidth,
            LayoutOrientation.Horizontal,
            LayoutOrientation.Vertical
        )
    }
    val VerticalMaxHeight: IntrinsicMeasureBlock = { measurables, availableWidth, _ ->
        intrinsicSize(
            measurables,
            { w -> maxIntrinsicHeight(w) },
            { h -> maxIntrinsicWidth(h) },
            availableWidth,
            LayoutOrientation.Vertical,
            LayoutOrientation.Vertical
        )
    }
}

private fun intrinsicSize(
    children: List<IntrinsicMeasurable>,
    intrinsicMainSize: IntrinsicMeasurable.(IntPx) -> IntPx,
    intrinsicCrossSize: IntrinsicMeasurable.(IntPx) -> IntPx,
    crossAxisAvailable: IntPx,
    layoutOrientation: LayoutOrientation,
    intrinsicOrientation: LayoutOrientation
) = if (layoutOrientation == intrinsicOrientation) {
    intrinsicMainAxisSize(children, intrinsicMainSize, crossAxisAvailable)
} else {
    intrinsicCrossAxisSize(children, intrinsicCrossSize, intrinsicMainSize, crossAxisAvailable)
}

private fun intrinsicMainAxisSize(
    children: List<IntrinsicMeasurable>,
    mainAxisSize: IntrinsicMeasurable.(IntPx) -> IntPx,
    crossAxisAvailable: IntPx
): IntPx {
    var weightUnitSpace = 0.ipx
    var fixedSpace = 0.ipx
    var totalWeight = 0f
    children.forEach { child ->
        val weight = child.weight
        val size = child.mainAxisSize(crossAxisAvailable)
        if (weight == 0f) {
            fixedSpace += size
        } else if (weight > 0f) {
            totalWeight += weight
            weightUnitSpace = max(weightUnitSpace, size / weight)
        }
    }
    return weightUnitSpace * totalWeight + fixedSpace
}

private fun intrinsicCrossAxisSize(
    children: List<IntrinsicMeasurable>,
    mainAxisSize: IntrinsicMeasurable.(IntPx) -> IntPx,
    crossAxisSize: IntrinsicMeasurable.(IntPx) -> IntPx,
    mainAxisAvailable: IntPx
): IntPx {
    var fixedSpace = 0.ipx
    var crossAxisMax = 0.ipx
    var totalWeight = 0f
    children.forEach { child ->
        val weight = child.weight
        if (weight == 0f) {
            val mainAxisSpace = child.mainAxisSize(IntPx.Infinity)
            fixedSpace += mainAxisSpace
            crossAxisMax = max(crossAxisMax, child.crossAxisSize(mainAxisSpace))
        } else if (weight > 0f) {
            totalWeight += weight
        }
    }

    val weightUnitSpace = if (totalWeight == 0f) {
        IntPx.Zero
    } else {
        max(mainAxisAvailable - fixedSpace, IntPx.Zero) / totalWeight
    }

    children.forEach { child ->
        if (child.weight > 0f) {
            crossAxisMax = max(crossAxisMax, child.crossAxisSize(weightUnitSpace * child.weight))
        }
    }
    return crossAxisMax
}

internal data class LayoutWeightImpl(val weight: Float, val fill: Boolean) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) =
        ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
            it.weight = weight
            it.fill = fill
        }
}

internal sealed class SiblingsAlignedModifier : ParentDataModifier {
    abstract override fun Density.modifyParentData(parentData: Any?): Any?

    internal data class WithAlignmentLineBlock(val block: (Measured) -> IntPx) :
        SiblingsAlignedModifier() {
        override fun Density.modifyParentData(parentData: Any?): Any? {
            return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
                it.crossAxisAlignment =
                    CrossAxisAlignment.Relative(AlignmentLineProvider.Block(block))
            }
        }
    }

    internal data class WithAlignmentLine(val line: AlignmentLine) :
        SiblingsAlignedModifier() {
        override fun Density.modifyParentData(parentData: Any?): Any? {
            return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
                it.crossAxisAlignment =
                    CrossAxisAlignment.Relative(AlignmentLineProvider.Value(line))
            }
        }
    }
}

internal data class GravityModifier(val alignment: Any) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): RowColumnParentData {
        return ((parentData as? RowColumnParentData) ?: RowColumnParentData()).also {
            it.crossAxisAlignment = alignment
        }
    }
}

/**
 * Parent data associated with children.
 */
internal data class RowColumnParentData(
    var weight: Float = 0f,
    var fill: Boolean = true,
    var crossAxisAlignment: Any? = null
)

/**
 * Provides the alignment line.
 */
internal sealed class AlignmentLineProvider {
    data class Block(val lineProviderBlock: (Measured) -> Int) : AlignmentLineProvider()
    data class Value(val line: AlignmentLine) : AlignmentLineProvider()
}