package mods.betterfoliage.common.config;

import java.util.Iterator;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;


public class NonVerboseArrayEntry extends GuiConfigEntries.ArrayEntry {

    @SuppressWarnings("unchecked")
    public NonVerboseArrayEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement<?> configElement) {
        super(owningScreen, owningEntryList, configElement);
        
        // strip defaults from tooltip as they can get quite long
        boolean defaultRows = false;
        Iterator<String> iter = toolTip.iterator();
        while(iter.hasNext()) {
            if (iter.next().startsWith(EnumChatFormatting.AQUA.toString())) defaultRows = true;
            if (defaultRows) iter.remove();
        }
        
        String shortDefaults = I18n.format("betterfoliage.arrayEntryDisplay", configElement.getDefaults().length);
        toolTip.addAll(this.mc.fontRenderer.listFormattedStringToWidth(EnumChatFormatting.AQUA + I18n.format("fml.configgui.tooltip.default", shortDefaults),300));
    }

    @Override
    public void updateValueButtonText() {
        this.btnValue.displayString = I18n.format("betterfoliage.arrayEntryDisplay", currentValues.length);
    }

}
