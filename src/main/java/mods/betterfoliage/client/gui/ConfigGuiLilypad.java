package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import mods.betterfoliage.client.gui.widget.OptionIntegerWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.FMLClientHandler;

public class ConfigGuiLilypad extends ConfigGuiScreenBase {

	public ConfigGuiLilypad(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.lilypadHOffset, -100, -40, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionIntegerWidget(BetterFoliage.config.lilypadChance, -100, -10, 200, 50, id++, id++, "message.betterfoliage.flowerChance"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 50, 100, 20, "Close"));
	}

	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
	}
	
}
