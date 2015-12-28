package mods.betterfoliage.client.gui

import cpw.mods.fml.client.config.GuiConfig
import cpw.mods.fml.client.config.GuiConfigEntries
import cpw.mods.fml.client.config.IConfigElement
import mods.octarinecore.client.gui.IdListConfigEntry
import net.minecraft.world.biome.BiomeGenBase

/** Toggleable list of all defined biomes. */
class BiomeListConfigEntry(
        owningScreen: GuiConfig,
        owningEntryList: GuiConfigEntries,
        configElement: IConfigElement<*>)
: IdListConfigEntry<BiomeGenBase>(owningScreen, owningEntryList, configElement) {

    override val baseSet: List<BiomeGenBase> get() = BiomeGenBase.getBiomeGenArray().filterNotNull()
    override val BiomeGenBase.itemId: Int get() = this.biomeID
    override val BiomeGenBase.itemName: String get() = this.biomeName
}