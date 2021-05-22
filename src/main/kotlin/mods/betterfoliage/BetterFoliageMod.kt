package mods.betterfoliage

import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.MainConfig
import mods.betterfoliage.util.tryDefault
import mods.octarinecore.common.config.clothGuiRoot
import mods.octarinecore.common.config.forgeSpecRoot
import net.alexwells.kottle.FMLKotlinModLoadingContext
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.ExtensionPoint.CONFIGGUIFACTORY
import net.minecraftforge.fml.ExtensionPoint.DISPLAYTEST
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import org.apache.commons.lang3.tuple.Pair
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.Properties
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Supplier

@Mod(BetterFoliageMod.MOD_ID)
object BetterFoliageMod {
    const val MOD_ID = "betterfoliage"

    val bus = FMLKotlinModLoadingContext.get().modEventBus
    val config = MainConfig()

    val detailLogStream = PrintStream(File("logs/betterfoliage.log").apply {
        parentFile.mkdirs()
        if (!exists()) createNewFile()
    })

    fun logger(obj: Any) = LogManager.getLogger(obj)
    fun detailLogger(obj: Any) = SimpleLogger(
        obj::class.java.simpleName, Level.DEBUG, false, true, true, false, "yyyy-MM-dd HH:mm:ss", null, PropertiesUtil(Properties()), detailLogStream
    )

    init {
        val ctx = ModLoadingContext.get()

        val configSpec = config.forgeSpecRoot()
        ctx.registerConfig(ModConfig.Type.CLIENT, configSpec)

        // Add config GUI extension if Cloth Config is available
        val clothLoaded = tryDefault(false) { Class.forName("me.shedaniel.forge.clothconfig2.api.ConfigBuilder"); true }
        if (clothLoaded) ctx.registerExtensionPoint(CONFIGGUIFACTORY) { BiFunction<Minecraft, Screen, Screen> { client, parent ->
            config.clothGuiRoot(
                parentScreen = parent,
                prefix = listOf(MOD_ID),
                background = ResourceLocation("minecraft:textures/block/spruce_log.png"),
                saveAction = { configSpec.save() }
            )
        } }

        // Accept-all version tester (we are client-only)
        ctx.registerExtensionPoint(DISPLAYTEST) {
            Pair.of(
                Supplier { "Honk if you see this!" },
                BiPredicate<String, Boolean> { _, _ -> true }
            )
        }

        Minecraft.getInstance().resourcePackList.addPackFinder(BetterFoliage.generatedPack.finder)
        bus.register(BlockConfig)
        BetterFoliage.init()
    }
}