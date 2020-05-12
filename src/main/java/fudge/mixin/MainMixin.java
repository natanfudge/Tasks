package fudge.mixin;

import fudge.mixinHandlers.Events;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {


    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;finishInitialization()V"))
    private static void afterMinecraftInit(String[] args, CallbackInfo ci){
        Events.INSTANCE.getOnWindowReady().invoker().invoke();
    }
}
