package fudge.gui


data class ComposableObject internal constructor(internal var modifier: Modifier, internal val debugName: String) {
    var parent: ComposableObject? = null

    internal val renderCallbacks = modifier.toList().filterIsInstance<RenderModifier>()
    internal val layoutModifier = modifier.toList().filterIsInstance<LayoutModifier>().lastOrNull()
    internal val sizeModifier = modifier.toList().filterIsInstance<SizeModifier>().firstOrNull() ?: SizeModifier.Default

    override fun toString(): String = debugName

    internal val children: MutableList<ComposableObject> = mutableListOf()

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

//    internal fun toConsoleTree(depth: Int = 0): String {
//        return " ".repeat(depth) + "{$debugName}" +
//                if (children.isNotEmpty()) ": [\n" + children.joinToString(",\n") { it.toConsoleTree(depth + 1) } + "\n" + " ".repeat(
//                    depth
//                ) + "]"
//                else ""
}

