package mods.betterfoliage.integration

import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxForge
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.allAvailable
import mods.betterfoliage.util.get
import mods.octarinecore.*
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration : HasLogger() {
    @JvmStatic val isAvailable = allAvailable(SVertexBuilder, SVertexBuilder.pushState, SVertexBuilder.popState, BlockAliases.getAliasBlockId)

    val defaultLeaves = Blocks.OAK_LEAVES.defaultState
    val defaultGrass = Blocks.GRASS.defaultState

    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockStateOverride(state: BlockState, world: ILightReader, pos: BlockPos): BlockState {
//        if (LeafRegistry[state, world, pos] != null) return defaultLeaves
//        if (BlockConfig.crops.matchesClass(state.block)) return defaultGrass
        return state
    }

    init {
        logger.log(INFO, "ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(buffer: BufferBuilder, state: BlockState, renderType: BlockRenderType, enabled: Boolean = true, func: ()->Unit) {
        if (isAvailable && enabled) {
            val aliasBlockId = BlockAliases.getAliasBlockId.invokeStatic(state)
            val sVertexBuilder = buffer[BufferBuilder_sVertexBuilder]
            SVertexBuilder.pushState.invoke(sVertexBuilder, aliasBlockId)
            func()
            SVertexBuilder.popState.invoke(sVertexBuilder)
        } else {
            func()
        }
    }

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(ctx: RenderCtxBase, enabled: Boolean = true, func: ()->Unit) =
        ((ctx as? RenderCtxVanilla)?.buffer as? BufferBuilder)?.let { bufferBuilder ->
            renderAs(bufferBuilder, defaultGrass, MODEL, enabled, func)
        }


    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(ctx: RenderCtxBase, enabled: Boolean = true, func: ()->Unit) =
        ((ctx as? RenderCtxVanilla)?.buffer as? BufferBuilder)?.let { bufferBuilder ->
            renderAs(bufferBuilder, defaultLeaves, MODEL, enabled, func)
        }
}
