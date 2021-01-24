import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.ExperimentalComposeApi
import fudge.gui.ComposableObject

@OptIn(ExperimentalComposeApi::class)
class NodeChangeApplier(
    root: ComposableObject,
    private val onChange: () -> Unit
) : AbstractApplier<ComposableObject>(root) {
    override fun insertBottomUp(index: Int, instance: ComposableObject) {
        current.insertAt(index, instance)
    }

    override fun insertTopDown(index: Int, instance: ComposableObject) {
        // ignored. Building tree bottom-up
    }

    override fun remove(index: Int, count: Int) {
        current.remove(index, count)
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.move(from, to, count)
    }

    override fun onClear() {
        root.removeAll()
    }

    override fun onEndChanges() : Unit =  onChange()
}
