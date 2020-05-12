package fudge.tasks;

import net.minecraft.entity.player.PlayerEntity;

@FunctionalInterface
public interface Reward {
    void provide(PlayerEntity rewardedPlayer, Task rewardingTask);
}
