package mods.betterfoliage.client

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.integration.ForestryIntegration
import mods.betterfoliage.client.integration.IC2RubberIntegration
import mods.betterfoliage.client.integration.OptifineCustomColors
import mods.betterfoliage.client.integration.TechRebornRubberIntegration
import mods.betterfoliage.client.render.*
import mods.betterfoliage.client.texture.*
import mods.octarinecore.client.gui.textComponent
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.resource.CenteringTextureGenerator
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

    // texture generators
    val genGrass = GrassGenerator("bf_gen_grass")
    val genLeaves = LeafGenerator("bf_gen_leaves")
    val genReeds = CenteringTextureGenerator("bf_gen_reeds", 1, 2)

    fun init() {
        // add resource generators to pack
        listOf(genGrass, genLeaves, genReeds).forEach { BetterFoliage.genPack.generators.add(it) }

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
            StandardCactusRegistry,
            LeafParticleRegistry,
            ChunkOverlayManager,
            LeafWindTracker,
            RisingSoulTextures
        )

        // init mod integrations
        val integrations = listOf(
//            ShadersModIntegration,
            OptifineCustomColors,
            ForestryIntegration,
            IC2RubberIntegration,
            TechRebornRubberIntegration
        )

        // add basic block support instances as last
        GrassRegistry.addRegistry(StandardGrassRegistry)
        LeafRegistry.addRegistry(StandardLeafRegistry)
        LogRegistry.addRegistry(StandardLogRegistry)

        configListeners = listOf(renderers, singletons, integrations).flatten().filterIsInstance<IConfigChangeListener>()
        configListeners.forEach { it.onConfigChange() }
    }

    fun log(level: Level, msg: String) {
        BetterFoliage.log.log(level, "[BetterFoliage] $msg")
        BetterFoliage.logDetail.log(level, msg)
    }

    fun logDetail(msg: String) {
        BetterFoliage.logDetail.log(Level.DEBUG, msg)
    }

    fun logRenderError(state: BlockState, location: BlockPos) {
        if (state in suppressRenderErrors) return
        suppressRenderErrors.add(state)

        val blockName = ForgeRegistries.BLOCKS.getKey(state.block).toString()
        val blockLoc = "${location.x},${location.y},${location.z}"
        Minecraft.getInstance().ingameGUI.chatGUI.printChatMessage(TranslationTextComponent(
            "betterfoliage.rendererror",
            textComponent(blockName, TextFormatting.GOLD),
            textComponent(blockLoc, TextFormatting.GOLD)
        ))
        logDetail("Error rendering block $state at $blockLoc")
    }
}

