package mods.betterfoliage

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.resource.AsnycSpriteProviderManager
import mods.octarinecore.client.resource.GeneratedBlockTexturePack
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.ParticleManager
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import org.apache.logging.log4j.Level.DEBUG
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.*

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