package mods.betterfoliage.client.gui;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiFactory implements IModGuiFactory {

	public void initialize(Minecraft minecraftInstance) {
		
	}

	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return ConfigGuiMain.class;
	}

	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return ImmutableSet.<RuntimeOptionCategoryElement>of();
	}

	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}

}
