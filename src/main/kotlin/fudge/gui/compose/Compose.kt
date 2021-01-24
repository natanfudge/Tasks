package fudge.gui.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot

internal object Compose {
    fun init() {
        Snapshot.registerGlobalWriteObserver { Snapshot.sendApplyNotifications() }
    }

    @OptIn(ExperimentalComposeApi::class)
    fun buildComposition(
        rootComposable: @Composable() () -> Unit,
        root: ComposableObject,
        onChange: () -> Unit
    ): Composition {
        val composition = compositionFor(root, NodeChangeApplier(root, onChange), Recomposer.current()) {
            println("Created Composition!")
        }

        composition.setContent(rootComposable)
        return composition
    }

}