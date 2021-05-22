package mods.betterfoliage

import me.shedaniel.forge.clothconfig2.api.ConfigBuilder
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.MainConfig
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
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Supplier

@Mod(BetterFoliageMod.MOD_ID)
object BetterFoliageMod {
    const val MOD_ID = "betterfoliage"

    val bus = FMLKotlinModLoadingContext.get().modEventBus
    val config = MainConfig()

    init {
        val ctx = ModLoadingContext.get()

        // Config instance + GUI handler
        val configSpec = config.forgeSpecRoot()
        ctx.registerConfig(ModConfig.Type.CLIENT, configSpec)
        ctx.registerExtensionPoint(CONFIGGUIFACTORY) { BiFunction<Minecraft, Screen, Screen> { client, parent ->
            config.clothGuiRoot(
                parentScreen = parent,
                prefix = listOf(MOD_ID),
                background = ResourceLocation("minecraft:textures/block/spruce_wood.png"),
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

        Minecraft.getInstance().resourcePackList.addPackFinder(BetterFoliage.asyncPack.finder)
        bus.register(BlockConfig)
        Client.init()
    }
}