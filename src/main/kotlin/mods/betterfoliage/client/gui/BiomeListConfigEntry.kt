package mods.betterfoliage.client.gui

import mods.octarinecore.client.gui.IdListConfigEntry
import net.minecraft.world.biome.Biome
import net.minecraftforge.fml.client.config.GuiConfig
import net.minecraftforge.fml.client.config.GuiConfigEntries
import net.minecraftforge.fml.client.config.IConfigElement

/** Toggleable list of all defined biomes. */
class BiomeListConfigEntry(
    owningScreen: GuiConfig,
    owningEntryList: GuiConfigEntries,
    configElement: IConfigElement)
: IdListConfigEntry<Biome>(owningScreen, owningEntryList, configElement) {

    override val baseSet: List<Biome> get() = Biome.REGISTRY.filterNotNull()
    override val Biome.itemId: Int get() = Biome.REGISTRY.getIDForObject(this)
    override val Biome.itemName: String get() = this.biomeName
}