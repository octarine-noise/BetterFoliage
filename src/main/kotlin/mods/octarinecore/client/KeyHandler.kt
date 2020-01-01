package mods.octarinecore.client

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.client.registry.ClientRegistry

class KeyHandler(val modId: String, val defaultKey: Int, val lang: String, val action: (InputEvent.KeyInputEvent)->Unit) {

    val keyBinding = KeyBinding(lang, defaultKey, modId)

    init {
        ClientRegistry.registerKeyBinding(keyBinding)
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun handleKeyPress(event: InputEvent.KeyInputEvent) {
        if (keyBinding.isPressed) action(event)
    }
}