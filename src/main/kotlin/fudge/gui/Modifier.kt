package fudge.gui

interface Modifier {
    fun <R> fold(acc: R, operation: (R, Modifier) -> R): R =
        operation(acc, this)

    companion object : Modifier {
        override fun <R> fold(acc: R, operation: (R, Modifier) -> R): R = acc
        fun fromList(modifiers: List<Modifier>): Modifier {
            if (modifiers.isEmpty()) return Modifier
            return fromList(modifiers, 0)
        }

        private fun fromList(modifiers: List<Modifier>, index: Int): Modifier {
            val currentModifier = modifiers[index]
            return if (index < modifiers.size - 1) {
                currentModifier wrap fromList(modifiers, index + 1)
            } else currentModifier
        }
    }
}

infix fun Modifier.wrap(other: Modifier): Modifier =
    if (other === Modifier) this else CombinedModifier(this, other)

class CombinedModifier(
    private val outer: Modifier,
    private val inner: Modifier
) : Modifier {
    override fun <R> fold(acc: R, operation: (R, Modifier) -> R): R =
        inner.fold(outer.fold(acc, operation), operation)
}

internal fun Modifier.toList(): List<Modifier> =
    fold(mutableListOf()) { list, modifier ->
        list.add(modifier)
        list
    }
