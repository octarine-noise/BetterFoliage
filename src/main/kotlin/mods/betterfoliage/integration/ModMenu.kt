package mods.betterfoliage.integration

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer
import me.shedaniel.clothconfig2.api.ConfigBuilder
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import net.minecraft.client.MinecraftClient
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.LiteralText
import java.nio.file.Files

object ModMenu : ModMenuApi {

    override fun getModConfigScreenFactory() = ConfigScreenFactory {
        val builder = ConfigBuilder.create()
                .setParentScreen(it)
                .setTitle(LiteralText(I18n.translate("betterfoliage.title")))
        BetterFoliage.config.createClothNode(listOf("betterfoliage")).value.forEach { rootOption ->
            builder.getOrCreateCategory(LiteralText("main")).addEntry(rootOption)
        }
        builder.savingRunnable = Runnable {
            FiberSerialization.serialize(BetterFoliage.config.fiberNode, Files.newOutputStream(BetterFoliage.configFile), JanksonValueSerializer(false))
            BakeWrapperManager.invalidate()
            MinecraftClient.getInstance().worldRenderer.reload()
        }
        builder.build()
    }

}