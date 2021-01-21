package fudge.tasks

//fun initClient(){}

//import fabricktx.api.initClientOnly
import Draw
import compose
import fabricktx.api.KotlinKeyBinding
import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.InputUtil
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.world.gen.GeneratorOptions
import org.lwjgl.glfw.GLFW


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
//        rootComponents.forEach { it.rebuild() }
    }
}


fun initClient() {
    println("Tasks initializing!")
    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
        KeyBindingRegistry.INSTANCE.register(refreshKey)
    }
//        registerKeyBinding(refreshKey)
    Events.OnWindowReady {

//        initDrawing(gui())

        val impl = DynamicRegistryManager.create()
        MinecraftClient.getInstance()
            .method_29607("Demo_World", MinecraftServer.DEMO_LEVEL_INFO, impl, GeneratorOptions.method_31112(impl))

        GlobalScope.launch {
            compose {
                Draw {
                    DrawableHelper.drawTextWithShadow(
                        matrices,
                        textRenderer,
                        Text.of("helloooooo"),
                        200,
                        200,
                        0x00_ff_ff_ff
                    )
                }
                Draw {
                    DrawableHelper.drawTextWithShadow(
                        matrices,
                        textRenderer,
                        Text.of("helloooooo"),
                        200,
                        100,
                        0x00_ff_00_ff
                    )
                }
            }
        }


//        MinecraftClient.getInstance().
    }

    Events.OnResolutionChanged {
//        rootComponents.forEach { it.rebuild() }
    }


//    HudRenderCallback.EVENT.register(HudRenderCallback { stack, delta ->
////        rootComponents.forEach {
////            it.draw(McCanvas(stack))
////        }
//    })
}

//fun gui(): Component {
//    return column(listOf(box(
//        height = 100,
//        width = 100,
////        rect = Rect.fromRTWH(right = ScreenUtils.width - 10, top = 50, height = 100, width = 80),
//        color = Color.Black.withOpacity(0x4f)
//    )))
//
//}
















