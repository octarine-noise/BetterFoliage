package mods.betterfoliage

import io.github.prospector.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.zeroeightsix.fiber.JanksonSettings
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.MainConfig
import mods.betterfoliage.render.block.vanilla.*
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.render.particle.RisingSoulParticle
import mods.betterfoliage.resource.discovery.BakedModelReplacer
import mods.betterfoliage.resource.generated.GeneratedBlockTexturePack
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.resource.language.I18n
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.*
import java.util.function.Function

object BetterFoliage : ClientModInitializer, ModMenuApi {
    const val MOD_ID = "betterfoliage"
    override fun getModId() = MOD_ID

    var logger = LogManager.getLogger()
    var logDetail = SimpleLogger(
        "BetterFoliage",
        Level.DEBUG,
        false, false, true, false,
        "yyyy-MM-dd HH:mm:ss",
        null,
        PropertiesUtil(Properties()),
        PrintStream(File(FabricLoader.getInstance().gameDirectory, "logs/betterfoliage.log").apply {
            parentFile.mkdirs()
            if (!exists()) createNewFile()
        })
    )

    val configFile get() = File(FabricLoader.getInstance().configDirectory, "BetterFoliage.json")

    val config = MainConfig().apply {
        if (configFile.exists()) JanksonSettings().deserialize(fiberNode, configFile.inputStream())
        else JanksonSettings().serialize(fiberNode, configFile.outputStream(), false)
    }

    val blockConfig = BlockConfig()
    override fun getConfigScreenFactory() = Function { screen: Screen ->
        val builder = ConfigBuilder.create()
            .setParentScreen(screen)
            .setTitle(I18n.translate("betterfoliage.title"))
        config.createClothNode(listOf("betterfoliage")).value.forEach { rootOption ->
            builder.getOrCreateCategory("main").addEntry(rootOption)
        }
        builder.savingRunnable = Runnable {
            JanksonSettings().serialize(config.fiberNode, configFile.outputStream(), false)
            modelReplacer.invalidate()
        }
        builder.build()
    }

    val generatedPack = GeneratedBlockTexturePack(Identifier(MOD_ID, "generated"), "betterfoliage-generated", "Better Foliage", "Generated leaf textures", logDetail)
    val modelReplacer = BakedModelReplacer()

    override fun onInitializeClient() {
        // Register generated resource pack
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(generatedPack)
        MinecraftClient.getInstance().resourcePackContainerManager.addCreator(generatedPack.finder)

        // Add standard block support
        modelReplacer.discoverers.add(StandardLeafDiscovery)
        modelReplacer.discoverers.add(StandardGrassDiscovery)
        modelReplacer.discoverers.add(StandardLogDiscovery)
        modelReplacer.discoverers.add(StandardCactusDiscovery)
        modelReplacer.discoverers.add(LilyPadDiscovery)
        modelReplacer.discoverers.add(DirtDiscovery)
        modelReplacer.discoverers.add(SandDiscovery)
        modelReplacer.discoverers.add(MyceliumDiscovery)
        modelReplacer.discoverers.add(NetherrackDiscovery)

        // Init overlay layers
        ChunkOverlayManager.layers.add(RoundLogOverlayLayer)

        // Init singletons
        LeafParticleRegistry
        NormalLeavesModel.Companion
        GrassBlockModel.Companion
        RoundLogModel.Companion
        CactusModel.Companion
        LilypadModel.Companion
        DirtModel.Companion
        SandModel.Companion
        MyceliumModel.Companion
        NetherrackModel.Companion
        RisingSoulParticle.Companion
    }

}