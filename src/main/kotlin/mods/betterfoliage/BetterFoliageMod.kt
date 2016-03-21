package mods.betterfoliage

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkCheckHandler
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Logger

@Mod(
    modid = BetterFoliageMod.MOD_ID,
    name = BetterFoliageMod.MOD_NAME,
    acceptedMinecraftVersions = BetterFoliageMod.MC_VERSIONS,
    guiFactory = BetterFoliageMod.GUI_FACTORY
)
object BetterFoliageMod {

    const val MOD_ID = "BetterFoliage"
    const val MOD_NAME = "Better Foliage"
    const val DOMAIN = "betterfoliage"
    const val LEGACY_DOMAIN = "bettergrassandleaves"
    const val MC_VERSIONS = "[1.9]"
    const val GUI_FACTORY = "mods.betterfoliage.client.gui.ConfigGuiFactory"

    lateinit var log: Logger
    lateinit var config: Configuration

    @JvmStatic
    @Mod.InstanceFactory
    // the fun never stops with the fun factory! :)
    fun factory() = this

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        log = event.modLog
        config = Configuration(event.suggestedConfigurationFile, null, true)

    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        if (FMLCommonHandler.instance().effectiveSide == Side.CLIENT) {
            Config.attach(config)
            Client.log(INFO, "BetterFoliage initialized")
        }
    }

    /** Mod is cosmetic only, always allow connection. */
    @NetworkCheckHandler
    fun checkVersion(mods: Map<String, String>, side: Side) = true
}