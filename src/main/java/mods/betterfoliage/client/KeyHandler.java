package mods.betterfoliage.client;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.ConfigGuiFactory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyHandler {
	
	public static KeyBinding guiBinding;
	
	public KeyHandler() {
		guiBinding = new KeyBinding("key.betterfoliage.gui", 66, BetterFoliage.MOD_NAME);
		ClientRegistry.registerKeyBinding(guiBinding);
	}
	
	@SubscribeEvent
	public void handleKeyPress(InputEvent.KeyInputEvent event) {
		if (guiBinding.isPressed()) FMLClientHandler.instance().showGuiScreen(new ConfigGuiFactory.ConfigGuiBetterFoliage(null));
	}
}
