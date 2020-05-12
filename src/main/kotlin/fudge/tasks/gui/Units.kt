package fudge.tasks.gui

import fudge.tasks.b

data class Color(val red: Byte, val green: Byte, val blue: Byte, val alpha: Byte) {
    companion object {
        val Black = Color(0x00.b, 0x00.b, 0x00.b, 0xff.b)
    }
}

fun Color.withOpacity(opacity: Byte) = Color(red, green, blue, opacity)


data class Rect(
    // The offset of the left edge of this rectangle from the x axis.
    val left: Int,
    // The offset of the top edge of this rectangle from the y axis.
    val top: Int,
    // The offset of the right edge of this rectangle from the x axis.
    val right: Int,
    // The offset of the bottom edge of this rectangle from the y axis.
    val bottom: Int
) {
    init {
        require(left <= right)
        require(top <= bottom)
    }

    companion object {
        /** Construct a rectangle from its left, top, right, and bottom edges. */
        fun fromLTRB(left: Int, top: Int, right: Int, bottom: Int): Rect {
            return Rect(left, top, right, bottom)
        }

        /**
         * Construct a rectangle from its left and top edges, its width, and its
         * height.
         */
        fun fromLTWH(left: Int, top: Int, width: Int, height: Int): Rect {
            return Rect(left, top, left + width, top + height)
        }

        fun fromRTWH(right: Int, top: Int, width: Int, height: Int): Rect {
            return Rect(right - width, top, right, top + height)
        }
    }

}

fun Rect.getSize() = Size(height = height, width = width)
val Rect.width get() = right - left
val Rect.height get() = bottom - top


data class Size(val height: Int, val width: Int){
    companion object{
        val Zero = Size(0,0)
        val Infinity = Size(Int.MAX_VALUE,Int.MAX_VALUE)
    }
}
data class IntPos(val left: Int, val top: Int){
    companion object{
        val Origin = IntPos(0,0)
    }
}
data class FloatPos(val left: Float, val top: Float){
    companion object{
        val Origin = FloatPos(0f,0f)
    }
}
