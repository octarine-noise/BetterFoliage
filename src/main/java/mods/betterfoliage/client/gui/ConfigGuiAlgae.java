package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import mods.betterfoliage.client.gui.widget.OptionIntegerWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiAlgae extends ConfigGuiScreenBase {

	public ConfigGuiAlgae(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.algaeHOffset, -100, -70, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.algaeSize, -100, -40, 200, 50, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.algaeHeightMin, -100, -10, 200, 50, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.algaeHeightMax, -100, 20, 200, 50, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
		widgets.add(new OptionIntegerWidget(BetterFoliage.config.algaeChance, -100, 50, 200, 50, id++, id++, "message.betterfoliage.algaeChance"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 100, 100, 20, I18n.format("message.betterfoliage.back")));
	}

	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
		
		if (BetterFoliage.config.algaeHeightMin.value > BetterFoliage.config.algaeHeightMax.value) BetterFoliage.config.algaeHeightMin.value = BetterFoliage.config.algaeHeightMax.value;
	}
	
}
