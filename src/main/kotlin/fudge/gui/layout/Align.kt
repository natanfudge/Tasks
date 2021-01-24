package fudge.gui.layout

data class Align(val x: Double, val y: Double) {
    init {
        require(x in 0.0..1.0)
        require(y in 0.0..1.0)
    }

    companion object {
        val TopLeft = Align(0.0,0.0)
        val TopCenter = Align(0.5,0.0)
        val TopRight = Align(1.0,0.0)
        val CenterLeft = Align(0.0,0.5)
        val Center = Align(0.5,0.5)
        val CenterRight = Align(1.0,0.5)
        val BottomLeft = Align(0.0,1.0)
        val BottomCenter = Align(0.5,1.0)
        val BottomRight = Align(1.0, 1.0)
    }
}
