package fudge.tasks

//fun initClient(){}

import fabricktx.api.KotlinKeyBinding
import fabricktx.api.initClientOnly
import fudge.mixinHandlers.Events
import fudge.mixinHandlers.invoke
import fudge.tasks.gui.*
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW


fun initDrawing(root: Component) {
    rootComponents.add(ComponentManager(root))
}

private val rootComponents = mutableListOf<ComponentManager>()

val refreshKey = KotlinKeyBinding.create(
    id = Identifier("ladder-ui","rebuild"),
    type = InputUtil.Type.KEYSYM,
    category = "ladder-ui-key-category",
    key = GLFW.GLFW_KEY_B
){
    onReleased {
        rootComponents.forEach { it.rebuild() }
    }
}



fun initClient() = initClientOnly(Tasks.ModId) {
    println("Tasks initializing!")
    if (FabricLoader.getInstance().isDevelopmentEnvironment) registerKeyBinding(refreshKey)
    Events.OnWindowReady {
        initDrawing(gui())
    }

    Events.OnResolutionChanged {
        rootComponents.forEach { it.rebuild() }
    }

    HudRenderCallback.EVENT.register(HudRenderCallback {stack, delta ->
        rootComponents.forEach {
            it.draw(McCanvas(stack))
        }
    })
}

fun gui(): Component {
    return column(listOf(box(
        height = 100,
        width = 100,
//        rect = Rect.fromRTWH(right = ScreenUtils.width - 10, top = 50, height = 100, width = 80),
        color = Color.Black.withOpacity(0x4f)
    )))

}
















