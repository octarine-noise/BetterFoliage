package mods.betterfoliage

import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.integration.*
import mods.betterfoliage.render.*
import mods.betterfoliage.render.block.vanillaold.AsyncCactusDiscovery
import mods.betterfoliage.render.block.vanillaold.AsyncLogDiscovery
import mods.betterfoliage.render.block.vanillaold.RenderAlgae
import mods.betterfoliage.render.block.vanillaold.RenderCactus
import mods.betterfoliage.render.block.vanillaold.RenderConnectedGrass
import mods.betterfoliage.render.block.vanillaold.RenderConnectedGrassLog
import mods.betterfoliage.render.block.vanillaold.RenderCoral
import mods.betterfoliage.render.block.vanillaold.RenderGrass
import mods.betterfoliage.render.block.vanillaold.RenderLeaves
import mods.betterfoliage.render.block.vanillaold.RenderLilypad
import mods.betterfoliage.render.block.vanillaold.RenderLog
import mods.betterfoliage.render.block.vanillaold.RenderMycelium
import mods.betterfoliage.render.block.vanillaold.RenderNetherrack
import mods.betterfoliage.render.block.vanillaold.RenderReeds
import mods.betterfoliage.texture.AsyncGrassDiscovery
import mods.betterfoliage.texture.AsyncLeafDiscovery
import mods.betterfoliage.texture.LeafParticleRegistry
import mods.betterfoliage.render.old.RenderDecorator
import mods.betterfoliage.resource.IConfigChangeListener
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

