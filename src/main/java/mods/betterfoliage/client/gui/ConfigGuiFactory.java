package mods.betterfoliage.client.gui;

import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiFactory implements IModGuiFactory {

	public void initialize(Minecraft minecraftInstance) {
		
	}

	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ConfigGuiBetterFoliage.class;
	}

	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return ImmutableSet.<RuntimeOptionCategoryElement>of();
	}

	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

	public static class ConfigGuiBetterFoliage extends GuiConfig {
		public ConfigGuiBetterFoliage(GuiScreen parentScreen) {
			super(parentScreen, Config.getConfigRootCategories(), BetterFoliage.MOD_ID, null, false, false, BetterFoliage.MOD_NAME);
		}
	}
}
