package mods.betterfoliage.integration

import io.github.prospector.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.zeroeightsix.fiber.JanksonSettings
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.LiteralText
import java.util.function.Function

object ModMenu : ModMenuApi {
    override fun getModId() = BetterFoliage.MOD_ID

    override fun getConfigScreenFactory() = Function { screen: Screen ->
        val builder = ConfigBuilder.create()
            .setParentScreen(screen)
            .setTitle(LiteralText(I18n.translate("betterfoliage.title")))
        BetterFoliage.config.createClothNode(listOf("betterfoliage")).value.forEach { rootOption ->
            builder.getOrCreateCategory(LiteralText("main")).addEntry(rootOption)
        }
        builder.savingRunnable = Runnable {
            JanksonSettings().serialize(BetterFoliage.config.fiberNode, BetterFoliage.configFile.outputStream(), false)
            BakeWrapperManager.invalidate()
            MinecraftClient.getInstance().worldRenderer.reload()
        }
        builder.build()
    }
}