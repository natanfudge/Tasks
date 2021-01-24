package fudge.mixinHandlers

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.world.World


private inline fun <reified T> event(noinline invokerFactory: (Array<T>) -> T): Event<T> {
    return EventFactoryImpl.createArrayBacked(T::class.java, invokerFactory)
}

private inline operator fun <T> Array<T>.invoke(callback: T.() -> Unit) = forEach(callback)

private fun noArgs() = event<() -> Unit> { listeners ->
    {
        listeners { invoke() }
    }
}

private val subscribedHudListeners = mutableListOf<HudRenderFunction>()


operator fun <T> Event<T>.invoke(listener: T) = register(listener)

typealias HudRenderFunction = (MatrixStack, Float) -> Unit

operator fun Event<HudRenderCallback>.invoke(listener: HudRenderFunction): HudRenderFunction {
    if (subscribedHudListeners.isEmpty()) {
        register { stack, delta ->
            for (existingListener in subscribedHudListeners) {
                existingListener(stack, delta)
            }
        }
    }
    subscribedHudListeners.add(listener)
    return listener
}

fun HudRenderFunction.unsubscribe(): Boolean = (subscribedHudListeners as MutableCollection<*>).remove(this)

// class EventSubscriptionKey

object Events {


    val OnWindowReady = noArgs()
    val OnResolutionChanged = noArgs()
    val OnHudRender: Event<HudRenderCallback> = HudRenderCallback.EVENT


    val OnBlockBroken = event<(World, PlayerEntity) -> Unit> { listeners ->
        { world, player ->
            listeners { invoke(world, player) }
        }

    }
}
