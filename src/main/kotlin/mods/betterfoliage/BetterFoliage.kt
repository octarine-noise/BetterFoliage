package mods.betterfoliage

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.resource.GeneratorPack
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.client.Minecraft
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

@Mod(BetterFoliage.MOD_ID)
object BetterFoliage {
    const val MOD_ID = ""
    const val MOD_NAME = "Better Foliage"

    val modBus = FMLKotlinModLoadingContext.get().modEventBus

    var log = LogManager.getLogger("BetterFoliage")
    var logDetail = SimpleLogger(
        "BetterFoliage",
        DEBUG,
        false, false, true, false,
        "yyyy-MM-dd HH:mm:ss",
        null,
        PropertiesUtil(Properties()),
        PrintStream(File("logs/betterfoliage.log").apply {
            parentFile.mkdirs()
            if (!exists()) createNewFile()
        })
    )

    val genPack = GeneratorPack(
        "bf_gen",
        "Better Foliage generated assets",
        "bf_generated_pack.png"
    )

    init {
        log.log(DEBUG, "Constructing mod")
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.build())
        Minecraft.getInstance().resourcePackList.addPackFinder(genPack.packFinder)
        Client.init()
    }
}