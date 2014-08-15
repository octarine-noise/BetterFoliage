package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;

public class ConfigGuiFallingLeaves extends ConfigGuiScreenBase {

	public ConfigGuiFallingLeaves(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesSize, -100, -100, 200, 50, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesSpeed, -100, -70, 200, 50, id++, id++, "message.betterfoliage.speed", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesWindStrength, -100, -40, 200, 50, id++, id++, "message.betterfoliage.windStrength", "%.1f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesStormStrength, -100, -10, 200, 50, id++, id++, "message.betterfoliage.stormStrength", "%.1f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesPerturb, -100, 20, 200, 50, id++, id++, "message.betterfoliage.fallingLeafPerturbation", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesChance, -100, 50, 200, 50, id++, id++, "message.betterfoliage.fallingLeafChance", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.fallingLeavesLifetime, -100, 80, 200, 50, id++, id++, "message.betterfoliage.fallingLeafLifetime", "%.2f"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(0, x - 50, y + 110, 100, 20, I18n.format("message.betterfoliage.back")));
	}
	
	@Override
	protected void onButtonPress(int id) {
		if (id == 0) FMLClientHandler.instance().showGuiScreen(parent);
	}
}
