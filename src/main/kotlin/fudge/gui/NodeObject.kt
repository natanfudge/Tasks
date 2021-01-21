import fudge.gui.Modifier
import fudge.gui.RenderModifier
import fudge.gui.toList

data class NodeObject internal constructor(
    private val modifier : Modifier
) {
    var parent: NodeObject? = null

    internal val renderCallbacks = modifier.toList().filterIsInstance<RenderModifier>()

    internal val children: MutableList<NodeObject> = mutableListOf()

    fun insertAt(index: Int, instance: NodeObject) {
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
//        return " ".repeat(depth) + "{$tag}" +
//                if (children.isNotEmpty()) ": [\n" + children.joinToString(",\n") { it.toConsoleTree(depth + 1) } + "\n" + " ".repeat(
//                    depth
//                ) + "]"
//                else ""
    }

