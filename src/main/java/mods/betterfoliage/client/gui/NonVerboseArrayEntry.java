package mods.betterfoliage.client.gui;

import mods.betterfoliage.client.util.MiscUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NonVerboseArrayEntry extends GuiConfigEntries.ArrayEntry {

    @SuppressWarnings("unchecked")
    public NonVerboseArrayEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        
        MiscUtils.stripTooltipDefaultText(toolTip);
        String shortDefaults = I18n.format("betterfoliage.arrayEntryDisplay", configElement.getDefaults().length);
        toolTip.addAll(this.mc.fontRendererObj.listFormattedStringToWidth(EnumChatFormatting.AQUA + I18n.format("fml.configgui.tooltip.default", shortDefaults),300));
    }

    @Override
    public void updateValueButtonText() {
        this.btnValue.displayString = I18n.format("betterfoliage.arrayEntryDisplay", currentValues.length);
    }

}
