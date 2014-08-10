package mods.betterfoliage.client.gui;

import java.util.List;

import mods.betterfoliage.client.gui.widget.IOptionWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigGuiScreenBase extends GuiScreen {

	protected GuiScreen parent;
	protected List<IOptionWidget> widgets = Lists.newLinkedList();
	
	public ConfigGuiScreenBase(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		int x = width / 2;
		int y = height / 2;
		for (IOptionWidget widget : widgets) widget.drawStrings(this, fontRendererObj, x, y, 14737632, 16777120);
		super.drawScreen(par1, par2, par3);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		int x = width / 2;
		int y = height / 2;
		for (IOptionWidget widget : widgets) widget.addButtons(buttonList, x, y);
		addButtons(x, y);
		updateButtons();
	}
	
	protected void addButtons(int x, int y) {}
	
	protected void updateButtons() {}
	
	protected void onButtonPress(int id) {}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		for (IOptionWidget widget : widgets) widget.onAction(button.id);
		onButtonPress(button.id);
		updateButtons();
	}
	
	@SuppressWarnings("unchecked")
	protected void setButtonOptionBoolean(int id, String msgKey, boolean option) {
		for (GuiButton button : (List<GuiButton>) buttonList) {
			if (button.id == id) {
				String optionText = option ? (EnumChatFormatting.GREEN + I18n.format("message.betterfoliage.optionOn")) : (EnumChatFormatting.RED + I18n.format("message.betterfoliage.optionOff"));
				button.displayString = I18n.format(msgKey, optionText);
				break;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void setButtonOptionBoolean(int id, String msgKey, String optionKey) {
		for (GuiButton button : (List<GuiButton>) buttonList) {
			if (button.id == id) {
				button.displayString = I18n.format(msgKey, I18n.format(optionKey));
				break;
			}
		}
	}
}
