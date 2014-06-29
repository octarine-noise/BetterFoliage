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

	public enum Button {CLOSE, TOGGLE_LEAVES, TOGGLE_GRASS, LEAVES_OFFSET_MODE, TOGGLE_CACTUS, TOGGLE_LILYPAD}
	
	private GuiScreen parent;
	protected List<IOptionWidget> widgets = Lists.newLinkedList();
	
	public ConfigGuiScreen(GuiScreen parent) {
		this.parent = parent;
		int id = 10;
		widgets.add(new OptionDoubleWidget(Config.leavesSize, -160, -40, 150, 40, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.leavesHOffset, -160, -10, 150, 40, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(Config.leavesVOffset, -160, 20, 150, 40, id++, id++, "message.betterfoliage.vOffset", "%.3f"));
		
		widgets.add(new OptionDoubleWidget(Config.grassSize, 10, -70, 150, 40, id++, id++, "message.betterfoliage.size", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.grassHOffset, 10, -40, 150, 40, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionDoubleWidget(Config.grassHeightMin, 10, -10, 150, 40, id++, id++, "message.betterfoliage.minHeight", "%.2f"));
		widgets.add(new OptionDoubleWidget(Config.grassHeightMax, 10, 20, 150, 40, id++, id++, "message.betterfoliage.maxHeight", "%.2f"));
		
		widgets.add(new OptionDoubleWidget(Config.lilypadHOffset, 10, 80, 150, 40, id++, id++, "message.betterfoliage.hOffset", "%.3f"));
		widgets.add(new OptionIntegerWidget(Config.lilypadChance, 10, 110, 150, 40, id++, id++, "message.betterfoliage.flowerChance"));
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int x = width / 2;
		int y = height / 2 - 30;
		for (IOptionWidget widget : widgets) widget.drawStrings(this, fontRendererObj, x, y, 14737632, 16777120);
		super.drawScreen(par1, par2, par3);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		int x = width / 2;
		int y = height / 2 - 30;
		for (IOptionWidget widget : widgets) widget.addButtons(buttonList, x, y);
		buttonList.add(new GuiButton(Button.CLOSE.ordinal(), x - 50, y + 130, 100, 20, "Close"));
		buttonList.add(new GuiButton(Button.TOGGLE_LEAVES.ordinal(), x - 160, y - 100, 150, 20, ""));
		buttonList.add(new GuiButton(Button.LEAVES_OFFSET_MODE.ordinal(), x - 160, y - 70, 150, 20, ""));
		buttonList.add(new GuiButton(Button.TOGGLE_GRASS.ordinal(), x + 10, y - 100, 150, 20, ""));
		buttonList.add(new GuiButton(Button.TOGGLE_CACTUS.ordinal(), x -160, y + 50, 150, 20, ""));
		buttonList.add(new GuiButton(Button.TOGGLE_LILYPAD.ordinal(), x + 10, y + 50, 150, 20, ""));
		updateButtons();
	}

	protected void updateButtons() {
		setButtonOptionBoolean(Button.TOGGLE_LEAVES, "message.betterfoliage.betterLeaves", Config.leavesEnabled);
		setButtonOptionBoolean(Button.LEAVES_OFFSET_MODE, "message.betterfoliage.leavesMode", Config.leavesSkew ? "message.betterfoliage.leavesSkew" : "message.betterfoliage.leavesTranslate");
		setButtonOptionBoolean(Button.TOGGLE_GRASS, "message.betterfoliage.betterGrass", Config.grassEnabled);
		setButtonOptionBoolean(Button.TOGGLE_CACTUS, "message.betterfoliage.betterCactus", Config.cactusEnabled);
		setButtonOptionBoolean(Button.TOGGLE_LILYPAD, "message.betterfoliage.betterLilypad", Config.lilypadEnabled);
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
		if (button.id == Button.LEAVES_OFFSET_MODE.ordinal()) Config.leavesSkew = !Config.leavesSkew;
		if (button.id == Button.TOGGLE_CACTUS.ordinal()) Config.cactusEnabled = !Config.cactusEnabled;
		if (button.id == Button.TOGGLE_LILYPAD.ordinal()) Config.lilypadEnabled = !Config.lilypadEnabled;
		
		for (IOptionWidget widget : widgets) widget.onAction(button.id);
		if (Config.grassHeightMin.value > Config.grassHeightMax.value) Config.grassHeightMin.value = Config.grassHeightMax.value;
		if (Config.grassHeightMin.value > Config.grassHeightMax.value) Config.grassHeightMax.value = Config.grassHeightMin.value;
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
	
	@SuppressWarnings("unchecked")
	protected void setButtonOptionBoolean(Button enumButton, String msgKey, String optionKey) {
		for (GuiButton button : (List<GuiButton>) buttonList) {
			if (button.id == enumButton.ordinal()) {
				button.displayString = I18n.format(msgKey, I18n.format(optionKey));
				break;
			}
		}
	}
}
