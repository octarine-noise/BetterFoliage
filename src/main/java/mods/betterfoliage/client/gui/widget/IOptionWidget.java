package mods.betterfoliage.client.gui.widget;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public interface IOptionWidget {

	public void addButtons(List<GuiButton> buttonList, int xOffset, int yOffset);
	public void drawStrings(GuiScreen screen, FontRenderer fontRenderer, int xOffset, int yOffset, int labelColor, int numColor);
	public void onAction(int buttonId, boolean shiftPressed);

}