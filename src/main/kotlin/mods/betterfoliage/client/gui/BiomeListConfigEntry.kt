package mods.betterfoliage.client.gui

import mods.octarinecore.client.gui.IdListConfigEntry
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.GuiConfigEntries
import net.minecraftforge.fml.client.config.IConfigElement

/** Toggleable list of all defined biomes. */
class BiomeListConfigEntry(
    owningScreen: GuiConfig,
    owningEntryList: GuiConfigEntries,
    configElement: IConfigElement)
: IdListConfigEntry<BiomeGenBase>(owningScreen, owningEntryList, configElement) {

    override val baseSet: List<BiomeGenBase> get() = BiomeGenBase.biomeRegistry.filterNotNull()
    override val BiomeGenBase.itemId: Int get() = BiomeGenBase.biomeRegistry.getIDForObject(this)
    override val BiomeGenBase.itemName: String get() = this.biomeName
}