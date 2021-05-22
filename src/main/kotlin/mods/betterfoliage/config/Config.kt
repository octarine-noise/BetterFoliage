package mods.betterfoliage.config

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ModelTextureListConfiguration
import net.minecraft.util.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.config.ModConfig
import java.util.Random

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
    private fun blocks(cfgName: String) = ConfigurableBlockMatcher(ResourceLocation(BetterFoliageMod.MOD_ID, cfgName)).apply { list.add(this) }
    private fun models(cfgName: String) = ModelTextureListConfiguration(ResourceLocation(BetterFoliageMod.MOD_ID, cfgName)).apply { list.add(this) }

    @SubscribeEvent
    fun onConfig(event: ModConfig.ModConfigEvent) {
        list.forEach { when(it) {
            is ConfigurableBlockMatcher -> it.readDefaults()
            is ModelTextureListConfiguration -> it.readDefaults()
        } }
    }
}
