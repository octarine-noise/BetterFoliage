package mods.betterfoliage.client.gui;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.widget.OptionDoubleWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.client.FMLClientHandler;

public class ConfigGuiLeaves extends ConfigGuiScreenBase {

	public enum Button {CLOSE, LEAVES_OFFSET_MODE}
	
	public ConfigGuiLeaves(GuiScreen parent) {
		super(parent);
		int id = 10;
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.leavesSize, -100, -70, 200, 50, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.leavesHOffset, -100, -10, 200, 50, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(BetterFoliage.config.leavesVOffset, -100, 20, 200, 50, id++, id++, "message.betterfoliage.vOffset", "%.3f"));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addButtons(int x, int y) {
		buttonList.add(new GuiButton(Button.CLOSE.ordinal(), x - 50, y + 50, 100, 20, I18n.format("message.betterfoliage.back")));
		buttonList.add(new GuiButton(Button.LEAVES_OFFSET_MODE.ordinal(), x - 100, y - 40, 200, 20, ""));
	}

	protected void updateButtons() {
		setButtonOptionBoolean(Button.LEAVES_OFFSET_MODE.ordinal(), "message.betterfoliage.leavesMode", BetterFoliage.config.leavesSkew ? "message.betterfoliage.leavesSkew" : "message.betterfoliage.leavesTranslate");
	}

	@Override
	protected void onButtonPress(int id) {
		if (id == Button.CLOSE.ordinal()) FMLClientHandler.instance().showGuiScreen(parent);
		if (id == Button.LEAVES_OFFSET_MODE.ordinal()) BetterFoliage.config.leavesSkew = !BetterFoliage.config.leavesSkew;
	}
	
}
