package mods.betterfoliage.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.ConfigGuiScreen;
import net.minecraft.client.settings.KeyBinding;

@SideOnly(Side.CLIENT)
public class KeyHandler {
	
	public static KeyBinding guiBinding;
	
	public KeyHandler() {
		guiBinding = new KeyBinding("key.betterfoliage.gui", 66, BetterFoliage.MOD_NAME);
		ClientRegistry.registerKeyBinding(guiBinding);
	}
	
	@SubscribeEvent
	public void handleKeyPress(InputEvent.KeyInputEvent event) {
		if (guiBinding.isPressed()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiScreen(null));
	}
}
