package mods.betterfoliage

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.isAfterPostInit
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Level.DEBUG
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.*

@Mod(
    modid = BetterFoliageMod.MOD_ID,
    name = BetterFoliageMod.MOD_NAME,
    acceptedMinecraftVersions = BetterFoliageMod.MC_VERSIONS,
    guiFactory = BetterFoliageMod.GUI_FACTORY,
    clientSideOnly = true
)
object BetterFoliageMod {

    const val MOD_ID = "betterfoliage"
    const val MOD_NAME = "Better Foliage"
    const val DOMAIN = "betterfoliage"
    const val LEGACY_DOMAIN = "bettergrassandleaves"
    const val MC_VERSIONS = "[1.12]"
    const val GUI_FACTORY = "mods.betterfoliage.client.gui.ConfigGuiFactory"

    lateinit var log: Logger
    lateinit var logDetail: Logger

    var config: Configuration? = null

    @JvmStatic
    @Mod.InstanceFactory
    // the fun never stops with the fun factory! :)
    fun factory(): BetterFoliageMod {
        // inject pack into default list at construction time to get domains enumerated
        // there's no 2nd resource reload pass anymore
        Client.generatorPack.inject()
        return this
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        log = event.modLog
        val logDetailFile = File(event.modConfigurationDirectory.parentFile, "logs/betterfoliage.log").apply {
            parentFile.mkdirs()
            if (!exists()) createNewFile()
        }
        logDetail = SimpleLogger(
            "BetterFoliage",
            DEBUG,
            false, false, true, false,
            "yyyy-MM-dd HH:mm:ss",
            null,
            PropertiesUtil(Properties()),
            PrintStream(logDetailFile)
        )
        config = Configuration(event.suggestedConfigurationFile, null, true)

        if (FMLCommonHandler.instance().effectiveSide == Side.CLIENT) {
            Config.attach(config!!)
            Client.init()
            Client.log(INFO, "BetterFoliage initialized")
            isAfterPostInit = true
        }
    }

    /** Mod is cosmetic only, always allow connection. */
    @NetworkCheckHandler
    fun checkVersion(mods: Map<String, String>, side: Side) = true
}
