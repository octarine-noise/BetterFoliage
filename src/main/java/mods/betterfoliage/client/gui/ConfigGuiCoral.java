package mods.betterfoliage.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import mods.betterfoliage.client.gui.widget.OptionIntegerWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class ConfigGuiCoral extends ConfigGuiScreenBase {

	public ConfigGuiCoral(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.coralCrustSize, -100, -100, 200, 50, id++, id++, "message.betterfoliage.crustSize", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.coralVOffset, -100, -70, 200, 50, id++, id++, "message.betterfoliage.vOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.coralSize, -100, -40, 200, 50, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.coralHOffset, -100, -10, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionIntegerWidget(BetterFoliage.config.coralPopulation, -100, 20, 200, 50, id++, id++, "message.betterfoliage.coralPopulation"));
		widgets.add(new OptionIntegerWidget(BetterFoliage.config.coralChance, -100, 50, 200, 50, id++, id++, "message.betterfoliage.coralChance"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 80, 100, 20, I18n.format("message.betterfoliage.back")));
	}
	
	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
	}
}
