package fudge.tasks

import net.minecraft.entity.player.PlayerEntity


fun Task.complete(completingPlayer : PlayerEntity) = reward.provide(completingPlayer,this)
fun PlayerEntity.completeTask(task: Task) = task.complete(this)