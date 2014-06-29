package mods.betterfoliage.client.gui;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public interface IOptionWidget {

	public void addButtons(List<GuiButton> buttonList, int xOffset, int yOffset);
	public void drawStrings(GuiScreen screen, FontRenderer fontRenderer, int xOffset, int yOffset, int labelColor, int numColor);
	public void onAction(int buttonId);

}