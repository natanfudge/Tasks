package fudge.mixinHandlers

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.impl.base.event.EventFactoryImpl
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

operator fun<T> Event<T>.invoke(listener : T)= register(listener)



object Events {


    val OnWindowReady = noArgs()
    val OnResolutionChanged = noArgs()

    val OnBlockBroken = event<(World, PlayerEntity) -> Unit> { listeners ->
        { world, player ->
            listeners { invoke(world, player) }
        }

    }
}
