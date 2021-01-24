package fudge.gui.layout

data class NodeSize(
    val minWidth: Int,
    val maxWidth: Int,
    val minHeight: Int,
    val maxHeight: Int
) {
    init {
        require(minWidth <= maxWidth)
        require(minHeight <= maxHeight)
    }

    private fun Int.safePlus(other: Int) =
        if (this == Int.MAX_VALUE || other == Int.MAX_VALUE) Int.MAX_VALUE else this + other

    operator fun plus(other: NodeSize) = NodeSize(
        minWidth.safePlus(other.minWidth),
        maxWidth.safePlus(other.maxWidth),
        minHeight.safePlus(other.minHeight),
        maxHeight.safePlus(other.maxHeight)
    )

    companion object {
        val Anything = NodeSize(0, Int.MAX_VALUE, 0, Int.MAX_VALUE)
        val Max = NodeSize(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)
    }
}
