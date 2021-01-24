package fudge.gui.compose

import fudge.gui.*
import fudge.gui.drawing.RenderModifier
import fudge.gui.layout.LayoutModifier
import fudge.gui.layout.SizeModifier


internal data class ComposableObject internal constructor(internal var modifier: Modifier, internal val debugName: String) {
    var parent: ComposableObject? = null

     val renderCallbacks = modifier.toList().filterIsInstance<RenderModifier>()
     val layoutModifier = modifier.toList().filterIsInstance<LayoutModifier>().lastOrNull()
     val sizeModifier = modifier.toList().filterIsInstance<SizeModifier>().firstOrNull() ?: SizeModifier.Default

    override fun toString(): String = debugName

     val children: MutableList<ComposableObject> = mutableListOf()

    fun insertAt(index: Int, instance: ComposableObject) {
        children.add(index, instance)
        instance.parent = this
    }

    fun removeAll() {
        children.clear()
    }

    fun move(from: Int, to: Int, count: Int) {
        if (from > to) {
            var current = to
            repeat(count) {
                val node = children[from]
                children.removeAt(from)
                children.add(current, node)
                current++
            }
        } else {
            repeat(count) {
                val node = children[from]
                children.removeAt(from)
                children.add(to - 1, node)
            }
        }
    }

    fun remove(index: Int, count: Int) {
        repeat(count) {
            val instance = children.removeAt(index)
            instance.parent = null
        }
    }
}

