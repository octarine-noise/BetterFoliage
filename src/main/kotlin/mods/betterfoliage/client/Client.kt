package mods.betterfoliage.client

import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.integration.OptifineCustomColors
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.render.AsyncCactusDiscovery
import mods.betterfoliage.client.render.AsyncLogDiscovery
import mods.betterfoliage.client.render.LeafWindTracker
import mods.betterfoliage.client.render.RenderAlgae
import mods.betterfoliage.client.render.RenderCactus
import mods.betterfoliage.client.render.RenderConnectedGrass
import mods.betterfoliage.client.render.RenderConnectedGrassLog
import mods.betterfoliage.client.render.RenderCoral
import mods.betterfoliage.client.render.RenderGrass
import mods.betterfoliage.client.render.RenderLeaves
import mods.betterfoliage.client.render.RenderLilypad
import mods.betterfoliage.client.render.RenderLog
import mods.betterfoliage.client.render.RenderMycelium
import mods.betterfoliage.client.render.RenderNetherrack
import mods.betterfoliage.client.render.RenderReeds
import mods.betterfoliage.client.render.RisingSoulTextures
import mods.betterfoliage.client.texture.AsyncGrassDiscovery
import mods.betterfoliage.client.texture.AsyncLeafDiscovery
import mods.betterfoliage.client.texture.LeafParticleRegistry
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.resource.IConfigChangeListener
import net.minecraft.block.BlockState

/**
 * Object responsible for initializing (and holding a reference to) all the infrastructure of the mod
 * except for the call hooks.
 */
object Client {
    var renderers = emptyList<RenderDecorator>()
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
            OptifineCustomColors
//            ForestryIntegration,
//            IC2RubberIntegration,
//            TechRebornRubberIntegration
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

