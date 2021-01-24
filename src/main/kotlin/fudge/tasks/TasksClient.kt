package fudge.tasks

//fun initClient(){}

//import fabricktx.api.initClientOnly
import Box
import align
import border
import compose
import fabricktx.api.KotlinKeyBinding
import fudge.gui.*
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
import refreshGui


//fun initDrawing(root: Component) {
//    rootComponents.add(ComponentManager(root))
//}

//private val rootComponents = mutableListOf<ComponentManager>()


val refreshKey = KotlinKeyBinding.create(
    id = Identifier("ladder-ui", "rebuild"),
    type = InputUtil.Type.KEYSYM,
    category = "ladder-ui-key-category",
    key = GLFW.GLFW_KEY_B
) {
    onReleased {
        refreshGui()
        //TODO: instead integrate hotkeys into the compose system and use it normally through a @composable
//        val recomposerClass = Composer::class
//        val currentRecomposeScope =
//            recomposerClass.declaredMemberProperties.first { it.name == "currentRecomposeScope" }
//        val recomposeScope = currentRecomposeScope.get(GuiCheats.currentComposer!!)
//        val recomposeScopeClass = Class.forName("androidx.compose.runtime.RecomposeScope").kotlin
//        val invalidate = recomposeScopeClass.declaredFunctions.first { it.name == "invalidate" }
//        invalidate.call(recomposeScope)
    }
}


fun initClient() {
    println("Tasks initializing!")
    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
        KeyBindingHelper.registerKeyBinding(refreshKey)
    }
    Events.OnWindowReady {
        val impl = DynamicRegistryManager.create()
        MinecraftClient.getInstance()
            .method_29607("Demo_World", MinecraftServer.DEMO_LEVEL_INFO, impl, GeneratorOptions.method_31112(impl))
        drawGui()

//        MinecraftGui.draw {
//        }
    }

    Events.OnResolutionChanged {
//        rootComponents.forEach { it.rebuild() }
    }

}

private fun drawGui() {
    compose {
        Box(
            Color.Black,
            Modifier.size(400,200)
                .border(1, Color.Red)
                .fillMaxSize()
                .align(Alignment(0.5,0.5))
//                .size(200,100)
                .border(1,Color.Blue)
                .size(200,100)
        )
    }
}


















