package mods.octarinecore.client

import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.InputEvent
import net.minecraft.client.settings.KeyBinding

class KeyHandler(val modId: String, val defaultKey: Int, val lang: String, val action: (InputEvent.KeyInputEvent)->Unit) {

    val keyBinding = KeyBinding(lang, defaultKey, modId)

    init {
        ClientRegistry.registerKeyBinding(keyBinding)
        FMLCommonHandler.instance().bus().register(this)
    }

    @SubscribeEvent
    fun handleKeyPress(event: InputEvent.KeyInputEvent) {
        if (keyBinding.isPressed) action(event)
    }
}