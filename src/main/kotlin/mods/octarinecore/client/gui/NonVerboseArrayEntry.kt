package mods.octarinecore.client.gui

import net.minecraft.client.resources.I18n
import net.minecraft.util.text.TextFormatting.*
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.GuiConfigEntries
import net.minecraftforge.fml.client.config.IConfigElement

class NonVerboseArrayEntry(
    owningScreen: GuiConfig,
    owningEntryList: GuiConfigEntries,
    configElement: IConfigElement
) : GuiConfigEntries.ArrayEntry(owningScreen, owningEntryList, configElement) {

    init {
        stripTooltipDefaultText(toolTip as MutableList<String>)
        val shortDefaults = I18n.format("${configElement.languageKey}.arrayEntry", configElement.defaults.size)
        toolTip.addAll(mc.fontRendererObj.listFormattedStringToWidth("$AQUA${I18n.format("fml.configgui.tooltip.default", shortDefaults)}", 300))
    }

    override fun updateValueButtonText() {
        btnValue.displayString = I18n.format("${configElement.languageKey}.arrayEntry", currentValues.size)
    }

}