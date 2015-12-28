package mods.octarinecore.client.gui

import cpw.mods.fml.client.config.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.util.EnumChatFormatting.GOLD
import net.minecraft.util.EnumChatFormatting.YELLOW

/**
 * Base class for a config GUI element.
 * The GUI representation is a list of toggleable objects.
 * The config representation is an integer list of the selected objects' IDs.
 */
abstract class IdListConfigEntry<T>(
        owningScreen: GuiConfig,
        owningEntryList: GuiConfigEntries,
        configElement: IConfigElement<*>
) : GuiConfigEntries.CategoryEntry(owningScreen, owningEntryList, configElement) {

    /** Create the child GUI elements. */
    fun createChildren() = baseSet.map {
        ItemWrapperElement(it, it.itemId in configElement.list, it.itemId in configElement.defaults)
    }

    init { stripTooltipDefaultText(toolTip as MutableList<String>) }

    override fun buildChildScreen(): GuiScreen {
        return GuiConfig(this.owningScreen, createChildren(), this.owningScreen.modID,
                owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(), this.owningScreen.title,
                ((if (this.owningScreen.titleLine2 == null) "" else this.owningScreen.titleLine2) + " > " + this.name))
    }

    override fun saveConfigElement(): Boolean {
        val requiresRestart = (childScreen as GuiConfig).entryList.saveConfigElements()
        val children = (childScreen as GuiConfig).configElements as List<ItemWrapperElement>
        val ids = children.filter { it.booleanValue == true }.map { it.item.itemId }
        configElement.set(ids.sorted().toTypedArray())
        return requiresRestart
    }

    abstract val baseSet: List<T>
    abstract val T.itemId: Int
    abstract val T.itemName: String

    /** Child config GUI element of a single toggleable object. */
    inner class ItemWrapperElement(val item: T, value: Boolean, val default: Boolean) :
            DummyConfigElement<Boolean>(item.itemName, default, ConfigGuiType.BOOLEAN, item.itemName) {
        init { set(value) }

        override fun getComment() = I18n.format("${configElement.languageKey}.tooltip.element", "${GOLD}${item.itemName}${YELLOW}")
        override fun set(value: Boolean) { this.value = value }
        fun setDefault(value: Boolean) { this.defaultValue = value }
        val booleanValue: Boolean get() = value as Boolean
    }


}