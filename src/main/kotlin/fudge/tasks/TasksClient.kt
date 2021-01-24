package fudge.tasks

import fabricktx.api.KotlinKeyBinding
import fudge.gui.*
import fudge.gui.compose.FabricateDebug
import fudge.gui.drawing.Color
import fudge.gui.layout.Align
import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.gen.GeneratorOptions
import org.lwjgl.glfw.GLFW


val refreshKey = KotlinKeyBinding.create(
    id = Identifier("ladder-ui", "rebuild"),
    type = InputUtil.Type.KEYSYM,
    category = "ladder-ui-key-category",
    key = GLFW.GLFW_KEY_B
) {
    onReleased {
        FabricateDebug.refreshGui()
    }
}

val toggleDebugKey = KotlinKeyBinding.create(
    id = Identifier("fabricated-ui","debug-borders"),
    type = InputUtil.Type.KEYSYM,
    category = "fabricated-ui-key-category",
    key = GLFW.GLFW_KEY_O
){
    onReleased {
        FabricateDebug.toggleBorders()
    }
}


fun initClient() {
    println("Tasks initializing!")
    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
        KeyBindingHelper.registerKeyBinding(refreshKey)
        KeyBindingHelper.registerKeyBinding(toggleDebugKey)
    }
    Events.OnWindowReady {
        val impl = DynamicRegistryManager.create()
        MinecraftClient.getInstance()
            .method_29607("Demo_World", MinecraftServer.DEMO_LEVEL_INFO, impl, GeneratorOptions.method_31112(impl))
        drawGui()

    }

    Events.OnResolutionChanged {
//        rootComponents.forEach { it.rebuild() }
    }

}

private fun drawGui() {
    FabricateUi.compose {
//        TextElement("hello", Color.White, Modifier.padding(3))
        BoxElement(
            Color.Black.withOpacity(127),
            Modifier
                .fillMaxSize()
                .align(Align.TopRight)
                .padding(3)
                .size(100,150)

        )
    }
}


















