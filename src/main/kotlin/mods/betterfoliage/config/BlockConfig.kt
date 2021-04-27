package mods.betterfoliage.config

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.util.Invalidator
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ModelTextureListConfiguration
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

class BlockConfig  {
    private val list = mutableListOf<Any>()

    val leafBlocks = blocks("leaves_blocks_default.cfg")
    val leafModels = models("leaves_models_default.cfg")
    val grassBlocks = blocks("grass_blocks_default.cfg")
    val grassModels = models("grass_models_default.cfg")
//    val mycelium = blocks("mycelium_blocks_default.cfg")
//    val dirt = blocks("dirt_default.cfg")
//    val crops = blocks("crop_default.cfg")
    val logBlocks = blocks("log_blocks_default.cfg")
    val logModels = models("log_models_default.cfg")
//    val sand = blocks("sand_default.cfg")
//    val lilypad = blocks("lilypad_default.cfg")
//    val cactus = blocks("cactus_default.cfg")
//    val netherrack = blocks("netherrack_blocks_default.cfg")

    private fun blocks(cfgName: String) = ConfigurableBlockMatcher(BetterFoliage.logDetail, Identifier(BetterFoliage.MOD_ID, cfgName)).apply { list.add(this) }
    private fun models(cfgName: String) = ModelTextureListConfiguration(BetterFoliage.logDetail, Identifier(BetterFoliage.MOD_ID, cfgName)).apply { list.add(this) }

    fun reloadConfig(manager: ResourceManager) {
        list.forEach { when(it) {
            is ConfigurableBlockMatcher -> it.readDefaults(manager)
            is ModelTextureListConfiguration -> it.readDefaults(manager)
        } }
    }
}