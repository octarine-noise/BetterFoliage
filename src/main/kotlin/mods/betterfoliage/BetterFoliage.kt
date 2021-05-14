package mods.betterfoliage

import me.zeroeightsix.fiber.JanksonSettings
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.MainConfig
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.render.block.vanilla.*
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.render.particle.RisingSoulParticle
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.BlockTypeCache
import mods.betterfoliage.resource.generated.GeneratedBlockTexturePack
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.mixin.resource.loader.ResourcePackManagerAccessor
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.simple.SimpleLogger
import org.apache.logging.log4j.util.PropertiesUtil
import java.io.File
import java.io.PrintStream
import java.util.*


object BetterFoliage : ClientModInitializer {
    const val MOD_ID = "betterfoliage"

    val detailLogStream = PrintStream(File("logs/betterfoliage.log").apply {
        parentFile.mkdirs()
        if (!exists()) createNewFile()
    })

    fun logger(obj: Any) = LogManager.getLogger(obj)
    fun detailLogger(obj: Any) = SimpleLogger(
        obj::class.java.simpleName, Level.DEBUG, false, true, true, false, "yyyy-MM-dd HH:mm:ss", null, PropertiesUtil(Properties()), detailLogStream
    )

    val configFile get() = File(FabricLoader.getInstance().configDirectory, "BetterFoliage.json")

    val config = MainConfig().apply {
        if (configFile.exists()) JanksonSettings().deserialize(fiberNode, configFile.inputStream())
        else JanksonSettings().serialize(fiberNode, configFile.outputStream(), false)
    }

    val blockConfig = BlockConfig()
    val generatedPack = GeneratedBlockTexturePack(Identifier(MOD_ID, "generated"), "betterfoliage-generated", "Better Foliage", "Generated leaf textures")

    /** List of recognized [BlockState]s */
    var blockTypes = BlockTypeCache()

    override fun onInitializeClient() {
        // Register generated resource pack
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(generatedPack.reloader)
        (MinecraftClient.getInstance().resourcePackManager as ResourcePackManagerAccessor)
            .providers.add(generatedPack.finder)

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(blockConfig)

        // Add standard block support
        BakeWrapperManager.discoverers.add(StandardCactusDiscovery)
        BakeWrapperManager.discoverers.add(StandardDirtDiscovery)
        BakeWrapperManager.discoverers.add(StandardGrassDiscovery)
        BakeWrapperManager.discoverers.add(StandardLeafDiscovery)
        BakeWrapperManager.discoverers.add(StandardLilypadDiscovery)
        BakeWrapperManager.discoverers.add(StandardMyceliumDiscovery)
        BakeWrapperManager.discoverers.add(StandardNetherrackDiscovery)
        BakeWrapperManager.discoverers.add(StandardRoundLogDiscovery)
        BakeWrapperManager.discoverers.add(StandardSandDiscovery)

        // Init overlay layers
        ChunkOverlayManager.layers.add(RoundLogOverlayLayer)

        // Init singletons
        LeafParticleRegistry
        StandardLeafModel.Companion
        StandardGrassModel.Companion
        StandardRoundLogModel.Companion
        StandardCactusModel.Companion
        StandardLilypadModel.Companion
        DirtModel.Companion
        StandardSandModel.Companion
        StandardMyceliumModel.Companion
        StandardNetherrackModel.Companion
        RisingSoulParticle.Companion
        ShadersModIntegration
    }

}