package mods.betterfoliage.client.gui.widget;

import java.util.List;

import mods.betterfoliage.common.config.OptionInteger;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OptionIntegerWidget implements IOptionWidget {

	public OptionInteger option;
	public int x;
	public int y;
	public int width;
	public int numWidth;
	public int idDecrement;
	public int idIncrement;
	public String keyLabel;
	
	public OptionIntegerWidget(OptionInteger option, int x, int y, int width, int numWidth, int idDecrement, int idIncrement, String keyLabel) {
		this.option = option;
		this.x = x;
		this.y = y;
		this.width = width;
		this.numWidth = numWidth;
		this.idDecrement = idDecrement;
		this.idIncrement = idIncrement;
		this.keyLabel = keyLabel;
	}
	
	public void addButtons(List<GuiButton> buttonList, int xOffset, int yOffset) {
		buttonList.add(new GuiButton(idDecrement, xOffset + x + width - numWidth - 40, yOffset + y, 20, 20, "-"));
		buttonList.add(new GuiButton(idIncrement, xOffset + x + width - 20, yOffset + y, 20, 20, "+"));
	}
	
	public void drawStrings(GuiScreen screen, FontRenderer fontRenderer, int xOffset, int yOffset, int labelColor, int numColor) {
		screen.drawString(fontRenderer, I18n.format(keyLabel), xOffset + x, yOffset + y + 5, labelColor);
		screen.drawCenteredString(fontRenderer, Integer.toString(option.value), xOffset + x + width - 20 - numWidth / 2, yOffset + y + 5, numColor);
	}
	
	public void onAction(int buttonId, boolean shiftPressed) {
		if (buttonId == idDecrement) option.decrement(shiftPressed ? 5 :1);
		if (buttonId == idIncrement) option.increment(shiftPressed ? 5 :1);
	}
}
