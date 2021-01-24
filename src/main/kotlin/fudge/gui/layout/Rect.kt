package fudge.gui.layout

import kotlin.math.max
import kotlin.math.min

data class Rect(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun constrain(constraints: Rect) =
        if (x < constraints.x || y < constraints.y || width > constraints.width || height > constraints.height) {
            Rect(
                max(x, constraints.x),
                max(y, constraints.y),
                min(width, constraints.width),
                min(height, constraints.height)
            )
        } else this
}