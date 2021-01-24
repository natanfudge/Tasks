package fudge.gui.drawing

import fudge.gui.layout.Rect


internal fun DrawContext.renderBorder(
    left: Int,
    right: Int,
    top: Int,
    bottom: Int,
    color: Color,
) {
    // Fill left side (including corners)
    if (left > 0) canvas.fill(color, Rect(0, 0, left, size.height))
    // Fill top side (not including corners)
    if (top > 0) canvas.fill(color, Rect(left, 0, size.width - left - right, top))
    // Fill right side (including corners)
    if (right > 0) canvas.fill(color, Rect(size.width - right, 0, right, size.height))
    // Fill bottom side (not including corners)
    if (bottom > 0) canvas.fill(color, Rect(left, size.height - bottom, size.width - left - right, bottom))
}
