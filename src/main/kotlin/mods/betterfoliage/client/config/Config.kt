package mods.betterfoliage.client.config

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.common.config.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.config.ModConfig

object BlockConfig {
    private val list = mutableListOf<Any>()

    val leafBlocks = blocks("leaves_blocks_default.cfg")
    val leafModels = models("leaves_models_default.cfg")
    val grassBlocks = blocks("grass_blocks_default.cfg")
    val grassModels = models("grass_models_default.cfg")
    val mycelium = blocks("mycelium_blocks_default.cfg")
//    val dirt = blocks("dirt_default.cfg")
    val crops = blocks("crop_default.cfg")
    val logBlocks = blocks("log_blocks_default.cfg")
    val logModels = models("log_models_default.cfg")
    val lilypad = blocks("lilypad_default.cfg")

    init { BetterFoliageMod.bus.register(this) }
    private fun blocks(cfgName: String) = ConfigurableBlockMatcher(BetterFoliage.logDetail, ResourceLocation(BetterFoliageMod.MOD_ID, cfgName)).apply { list.add(this) }
    private fun models(cfgName: String) = ModelTextureListConfiguration(BetterFoliage.logDetail, ResourceLocation(BetterFoliageMod.MOD_ID, cfgName)).apply { list.add(this) }

    @SubscribeEvent
    fun onConfig(event: ModConfig.ModConfigEvent) {
        list.forEach { when(it) {
            is ConfigurableBlockMatcher -> it.readDefaults()
            is ModelTextureListConfiguration -> it.readDefaults()
        } }
    }
}