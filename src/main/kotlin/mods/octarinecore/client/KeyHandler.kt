package mods.octarinecore.client

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent

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