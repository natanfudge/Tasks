package fabricktx.api

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.minecraft.client.MinecraftClient
import net.minecraft.client.options.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW

@PublishedApi
internal const val Misc = "key.categories.misc"

typealias ClientCallback = (MinecraftClient) -> Unit

class KotlinKeyBindingBuilder @PublishedApi internal constructor(private val id: Identifier,
                                                                 private val code: Int,
                                                                 private val type: InputUtil.Type,
                                                                 private val category: String) {
    private var onPressStart: ClientCallback? = null
    private var onReleased: ClientCallback? = null

    @PublishedApi
    internal fun build() =
            KotlinKeyBinding(id, code, type, category, onPressStart, onReleased)

    ///////// API ///////////////

    fun onPressStart(callback: ClientCallback) = apply { onPressStart = callback }
    fun onReleased(callback: ClientCallback) = apply { onReleased = callback }

    //////////////////////////////
}

class KotlinKeyBinding(id: Identifier, code: Int, type: InputUtil.Type, category: String,
                       private val onPressStart: ClientCallback?, private val onReleased: ClientCallback?)
    : KeyBinding("key.${id.namespace}.${id.path}", type, code, category) {

    //////////////// API ////////////////
    companion object {
        /**
         * Must be registered with [ClientModInitializationContext.registerKeyBinding]
         * @param key keys are specified in [GLFW].*
         * @param type keyboard or mouse
         * @param init specify callbacks such as onPressStart and onReleased
         */
        inline fun create(id: Identifier,
                          key: Int,
                          type: InputUtil.Type = InputUtil.Type.KEYSYM,
                          category: String = Misc, init: KotlinKeyBindingBuilder.() -> Unit = {}) = KotlinKeyBindingBuilder(
            id, key, type, category
        ).apply(init).build()
    }
    /////////////////////////////////////////

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        if (onPressStart != null && pressed && !wasPressed()) {
            onPressStart.invoke(MinecraftClient.getInstance())
        }
        if (onReleased != null && !pressed && wasPressed()) {
            onReleased.invoke(MinecraftClient.getInstance())
        }
    }

}

