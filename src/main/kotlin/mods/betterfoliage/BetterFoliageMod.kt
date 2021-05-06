package mods.betterfoliage

import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.Properties

@Mod(BetterFoliageMod.MOD_ID)
object BetterFoliageMod {
    const val MOD_ID = "betterfoliage"

    val bus = FMLKotlinModLoadingContext.get().modEventBus

    val detailLogStream = PrintStream(File("logs/betterfoliage.log").apply {
        parentFile.mkdirs()
        if (!exists()) createNewFile()
    })

    fun logger(obj: Any) = LogManager.getLogger(obj)
    fun detailLogger(obj: Any) = SimpleLogger(
        obj::class.java.simpleName, Level.DEBUG, false, true, true, false, "yyyy-MM-dd HH:mm:ss", null, PropertiesUtil(Properties()), detailLogStream
    )

    init {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.build())
        Minecraft.getInstance().resourcePackList.addPackFinder(Client.asyncPack.finder)
        bus.register(BlockConfig)
        Client.init()
    }
}