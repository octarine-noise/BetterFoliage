package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import mods.betterfoliage.client.gui.widget.OptionIntegerWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;

public class ConfigGuiReed extends ConfigGuiScreenBase {

	public ConfigGuiReed(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.reedHOffset, -100, -70, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.reedHeightMin, -100, -40, 200, 50, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.reedHeightMax, -100, -10, 200, 50, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
		widgets.add(new OptionIntegerWidget(BetterFoliage.config.reedChance, -100, 20, 200, 50, id++, id++, "message.betterfoliage.reedChance"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 50, 100, 20, I18n.format("message.betterfoliage.back")));
	}

	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
		
		if (BetterFoliage.config.reedHeightMin.value > BetterFoliage.config.reedHeightMax.value) BetterFoliage.config.reedHeightMin.value = BetterFoliage.config.reedHeightMax.value;
	}
	
}
