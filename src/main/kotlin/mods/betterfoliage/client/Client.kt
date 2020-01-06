package mods.betterfoliage.client

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.integration.*
import mods.betterfoliage.client.render.*
import mods.betterfoliage.client.texture.AsyncGrassDiscovery
import mods.betterfoliage.client.texture.AsyncLeafDiscovery
import mods.betterfoliage.client.texture.LeafParticleRegistry
import mods.octarinecore.client.gui.textComponent
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.resource.IConfigChangeListener
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level

/**
 * Object responsible for initializing (and holding a reference to) all the infrastructure of the mod
 * except for the call hooks.
 */
object Client {
    var renderers= emptyList<RenderDecorator>()
    var configListeners = emptyList<IConfigChangeListener>()

    val suppressRenderErrors = mutableSetOf<BlockState>()

    fun init() {
        // init renderers
        renderers = listOf(
            RenderGrass(),
            RenderMycelium(),
            RenderLeaves(),
            RenderCactus(),
            RenderLilypad(),
            RenderReeds(),
            RenderAlgae(),
            RenderCoral(),
            RenderLog(),
            RenderNetherrack(),
            RenderConnectedGrass(),
            RenderConnectedGrassLog()
        )

        // init other singletons
        val singletons = listOf(
            BlockConfig,
            ChunkOverlayManager,
            LeafWindTracker,
            RisingSoulTextures
        )

        // init mod integrations
        val integrations = listOf(
            ShadersModIntegration,
            OptifineCustomColors,
            ForestryIntegration,
            IC2RubberIntegration,
            TechRebornRubberIntegration
        )

        LeafParticleRegistry.init()

        // add basic block support instances as last
        AsyncLeafDiscovery.init()
        AsyncGrassDiscovery.init()
        AsyncLogDiscovery.init()
        AsyncCactusDiscovery.init()

        configListeners = listOf(renderers, singletons, integrations).flatten().filterIsInstance<IConfigChangeListener>()
        configListeners.forEach { it.onConfigChange() }
    }
}

