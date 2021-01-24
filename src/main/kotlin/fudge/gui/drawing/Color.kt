package fudge.gui.drawing

import kotlin.random.Random

data class Color(val alpha: Int, val red: Int, val green: Int, val blue: Int) {
    init {
        require(alpha in 0..255)
        require(red in 0..255)
        require(green in 0..255)
        require(blue in 0..255)
    }

    internal fun toMcFormat(): Int = (alpha shl 24) + (red shl 16) + (green shl 8) + blue
    fun withOpacity(opacity: Int) = copy(alpha = opacity)

    companion object {
        fun opaque(red: Int, green: Int, blue: Int) = Color(255, red, green, blue)
        fun random() = opaque(Random.nextInt(0,255), Random.nextInt(0,255), Random.nextInt(0,255))

        val White = opaque(255,255,255)
        val Black = opaque(0, 0, 0)
        val Red = opaque(255, 0, 0)
        val Green = opaque(0, 255, 0)
        val Blue = opaque(0, 0, 255)
        val Orange = opaque(255, 127, 0)
        val Indigo = opaque(46, 43, 95)
        val  Violet = opaque(139,0,255)
    }
}