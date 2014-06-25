package mods.betterfoliage.client.gui;

import java.util.List;

import com.google.common.collect.Lists;

import mods.betterfoliage.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiScreen extends GuiScreen {

	public enum Button {CLOSE, TOGGLE_LEAVES, TOGGLE_GRASS}
	
	private GuiScreen parent;
	protected List<OptionDoubleWidget> widgets = Lists.newLinkedList();
	
	public ConfigGuiScreen(GuiScreen parent) {
		this.parent = parent;
		int id = 3;
		widgets.add(new OptionDoubleWidget(Config.leavesSize, -160, -65, 150, 40, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.leavesHOffset, -160, -35, 150, 40, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(Config.leavesVOffset, -160, -5, 150, 40, id++, id++, "message.betterfoliage.vOffset", "%.3f"));
		
		widgets.add(new OptionDoubleWidget(Config.grassSize, 10, -65, 150, 40, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.grassHOffset, 10, -35, 150, 40, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(Config.grassHeightMin, 10, -5, 150, 40, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.grassHeightMax, 10, 25, 150, 40, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int x = width / 2;
		int y = height / 2;
		for (OptionDoubleWidget widget : widgets) widget.drawStrings(this, fontRendererObj, x, y, 14737632, 16777120);
		super.drawScreen(par1, par2, par3);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		int x = width / 2;
		int y = height / 2;
		for (OptionDoubleWidget widget : widgets) widget.addButtons(buttonList, x, y);
		buttonList.add(new GuiButton(Button.CLOSE.ordinal(), x - 50, y + 100, 100, 20, "Close"));
		buttonList.add(new GuiButton(Button.TOGGLE_LEAVES.ordinal(), x - 160, y - 100, 150, 20, ""));
		buttonList.add(new GuiButton(Button.TOGGLE_GRASS.ordinal(), x + 10, y - 100, 150, 20, ""));
		updateButtons();
	}

	protected void updateButtons() {
		setButtonOptionBoolean(Button.TOGGLE_LEAVES, "message.betterfoliage.betterLeaves", Config.leavesEnabled);
		setButtonOptionBoolean(Button.TOGGLE_GRASS, "message.betterfoliage.betterGrass", Config.grassEnabled);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);

		if (button.id == Button.CLOSE.ordinal()) {
			Config.save();
			Minecraft.getMinecraft().renderGlobal.loadRenderers();
			FMLClientHandler.instance().showGuiScreen(parent);
		}
		if (button.id == Button.TOGGLE_LEAVES.ordinal()) Config.leavesEnabled = !Config.leavesEnabled;
		if (button.id == Button.TOGGLE_GRASS.ordinal()) Config.grassEnabled = !Config.grassEnabled;

		for (OptionDoubleWidget widget : widgets) {
			if (button.id == widget.idDecrement) widget.option.decrement();
			if (button.id == widget.idIncrement) widget.option.increment();
			if (widget.option == Config.grassHeightMin && Config.grassHeightMin.value > Config.grassHeightMax.value) Config.grassHeightMin.value = Config.grassHeightMax.value;
			if (widget.option == Config.grassHeightMax && Config.grassHeightMin.value > Config.grassHeightMax.value) Config.grassHeightMax.value = Config.grassHeightMin.value;
		}
		updateButtons();
	}

	@SuppressWarnings("unchecked")
	protected void setButtonOptionBoolean(Button enumButton, String msgKey, boolean option) {
		for (GuiButton button : (List<GuiButton>) buttonList) {
			if (button.id == enumButton.ordinal()) {
				String optionText = option ? (EnumChatFormatting.GREEN + I18n.format("message.betterfoliage.optionOn")) : (EnumChatFormatting.RED + I18n.format("message.betterfoliage.optionOff"));
				button.displayString = I18n.format(msgKey, optionText);
				break;
			}
		}
	}
	
}
