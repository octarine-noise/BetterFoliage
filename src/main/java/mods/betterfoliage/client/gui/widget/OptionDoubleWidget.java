package mods.betterfoliage.client.gui.widget;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.betterfoliage.common.config.OptionDouble;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class OptionDoubleWidget implements IOptionWidget {

	public OptionDouble option;
	public int x;
	public int y;
	public int width;
	public int numWidth;
	public int idDecrement;
	public int idIncrement;
	public String keyLabel;
	public String formatString;
	
	public OptionDoubleWidget(OptionDouble option, int x, int y, int width, int numWidth, int idDecrement, int idIncrement, String keyLabel, String formatString) {
		this.option = option;
		this.x = x;
		this.y = y;
		this.width = width;
		this.numWidth = numWidth;
		this.idDecrement = idDecrement;
		this.idIncrement = idIncrement;
		this.keyLabel = keyLabel;
		this.formatString = formatString;
	}
	
	public void addButtons(List<GuiButton> buttonList, int xOffset, int yOffset) {
		buttonList.add(new GuiButton(idDecrement, xOffset + x + width - numWidth - 40, yOffset + y, 20, 20, "-"));
		buttonList.add(new GuiButton(idIncrement, xOffset + x + width - 20, yOffset + y, 20, 20, "+"));
	}
	
	public void drawStrings(GuiScreen screen, FontRenderer fontRenderer, int xOffset, int yOffset, int labelColor, int numColor) {
		screen.drawString(fontRenderer, I18n.format(keyLabel), xOffset + x, yOffset + y + 5, labelColor);
		screen.drawCenteredString(fontRenderer, String.format(formatString, option.value), xOffset + x + width - 20 - numWidth / 2, yOffset + y + 5, numColor);
	}

	public void onAction(int buttonId, boolean shiftPressed) {
		if (buttonId == idDecrement) option.decrement(shiftPressed ? 5 :1);
		if (buttonId == idIncrement) option.increment(shiftPressed ? 5 :1);
	}
}
