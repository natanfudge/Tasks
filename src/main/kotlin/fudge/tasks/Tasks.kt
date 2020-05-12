package fudge.tasks


import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import java.util.*


object DefaultTasks {
    val destroy5Blocks = Task {
        Reward { player, _ ->
            player.giveItemStack(ItemStack(Items.COBBLESTONE))
        }
    }
}


object Tasks {
    const val ModId = "tasks"
}

@Suppress("unused")
fun init() {
    manageDestroyBlocksTask()
}

private fun manageDestroyBlocksTask() {
    val breakAmountByPlayer: MutableMap<UUID, Int> = mutableMapOf()
    Events.OnBlockBroken { world, player ->
        if (world.isClient) return@OnBlockBroken
        val newBreakAmount = breakAmountByPlayer.computeIfAbsent(player.uuid) { 0 } + 1
        breakAmountByPlayer[player.uuid] = newBreakAmount

        if (newBreakAmount < 5) {
            println("broke $newBreakAmount out of 5.")
        }

        if (newBreakAmount == 5) {
            player.completeTask(DefaultTasks.destroy5Blocks)
        }
    }
}

