package fudge.tasks;

import net.minecraft.entity.player.PlayerEntity;


public interface Task {
    Reward getReward();

    static void complete(Task task, PlayerEntity player) {
        TaskUtilsKt.complete(task, player);
    }
}

