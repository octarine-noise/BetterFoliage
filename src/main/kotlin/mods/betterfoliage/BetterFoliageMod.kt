package mods.betterfoliage

import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig

@Mod(BetterFoliageMod.MOD_ID)
object BetterFoliageMod {
    const val MOD_ID = "betterfoliage"

    val bus = FMLKotlinModLoadingContext.get().modEventBus

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.build())
        Minecraft.getInstance().resourcePackList.addPackFinder(BetterFoliage.asyncPack.finder)
        bus.register(BlockConfig)
        Client.init()
    }
}