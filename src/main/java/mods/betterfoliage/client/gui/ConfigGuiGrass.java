package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiGrass extends ConfigGuiScreenBase {

	public enum Button {CLOSE, GRASS_USE_GENERATED}
	
	public ConfigGuiGrass(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.grassSize, -100, -70, 200, 50, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.grassHOffset, -100, -10, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.grassHeightMin, -100, 20, 200, 50, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.grassHeightMax, -100, 50, 200, 50, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(Button.CLOSE.ordinal(), x - 50, y + 80, 100, 20, I18n.format("message.betterfoliage.back")));
		buttonList.add(new GuiButton(Button.GRASS_USE_GENERATED.ordinal(), x - 100, y - 40, 200, 20, ""));
	}

	protected void updateButtons() {
		setButtonOptionBoolean(Button.GRASS_USE_GENERATED.ordinal(), "message.betterfoliage.genShortgrass", BetterFoliage.config.grassUseGenerated);
	}
	
	@Override
	protected void onButtonPress(int id) {
		if (id == Button.CLOSE.ordinal()) FMLClientHandler.instance().showGuiScreen(parent);
		if (id == Button.GRASS_USE_GENERATED.ordinal()) BetterFoliage.config.grassUseGenerated = !BetterFoliage.config.grassUseGenerated;
		
		if (BetterFoliage.config.grassHeightMin.value > BetterFoliage.config.grassHeightMax.value) BetterFoliage.config.grassHeightMin.value = BetterFoliage.config.grassHeightMax.value;
	}
	
}
