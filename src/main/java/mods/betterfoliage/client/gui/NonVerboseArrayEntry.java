package mods.betterfoliage.client.gui;

import mods.betterfoliage.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;


public class NonVerboseArrayEntry extends GuiConfigEntries.ArrayEntry {

    @SuppressWarnings("unchecked")
    public NonVerboseArrayEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement) {
        super(owningScreen, owningEntryList, configElement);
        
        Utils.stripTooltipDefaultText(toolTip);
        String shortDefaults = I18n.format("betterfoliage.arrayEntryDisplay", configElement.getDefaults().length);
        toolTip.addAll(this.mc.fontRenderer.listFormattedStringToWidth(EnumChatFormatting.AQUA + I18n.format("fml.configgui.tooltip.default", shortDefaults),300));
    }

    @Override
    public void updateValueButtonText() {
        this.btnValue.displayString = I18n.format("betterfoliage.arrayEntryDisplay", currentValues.length);
    }

}
