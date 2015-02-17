package mods.betterfoliage.client.gui;

import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableSet;

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
			super(parentScreen, Config.getConfigRootElements(), BetterFoliage.MOD_ID, null, false, false, BetterFoliage.MOD_NAME);
		}
	}
}
