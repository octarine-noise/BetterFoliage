package mods.betterfoliage.client.gui;

import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import mods.betterfoliage.client.gui.widget.OptionIntegerWidget;
import mods.betterfoliage.common.config.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.FMLClientHandler;

public class ConfigGuiReed extends ConfigGuiScreenBase {

	public ConfigGuiReed(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(Config.reedHOffset, -100, -70, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(Config.reedHeightMin, -100, -40, 200, 50, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.reedHeightMax, -100, -10, 200, 50, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
		widgets.add(new OptionIntegerWidget(Config.reedChance, -100, 20, 200, 50, id++, id++, "message.betterfoliage.reedChance"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 50, 100, 20, "Close"));
	}

	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
		
		if (Config.reedHeightMin.value > Config.reedHeightMax.value) Config.reedHeightMin.value = Config.reedHeightMax.value;
	}
	
}
