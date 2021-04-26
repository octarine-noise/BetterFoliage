package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.texture.LeafRegistry
import mods.octarinecore.*
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.metaprog.allAvailable
import mods.octarinecore.metaprog.get
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IEnviromentBlockReader
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration {

    @JvmStatic val isAvailable = allAvailable(SVertexBuilder, SVertexBuilder.pushState, SVertexBuilder.popState, BlockAliases.getAliasBlockId)

    val defaultLeaves = Blocks.OAK_LEAVES.defaultState
    val defaultGrass = Blocks.GRASS.defaultState

    /**
     * Called from transformed ShadersMod code.
     * @see mods.betterfoliage.loader.BetterFoliageTransformer
     */
    @JvmStatic fun getBlockStateOverride(state: BlockState, world: IEnviromentBlockReader, pos: BlockPos): BlockState {
        if (LeafRegistry[state, world, pos] != null) return defaultLeaves
        if (BlockConfig.crops.matchesClass(state.block)) return defaultGrass
        return state
    }

    init {
        BetterFoliage.log(INFO, "ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    inline fun renderAs(ctx: CombinedContext, renderType: BlockRenderType, enabled: Boolean = true, func: ()->Unit) =
        renderAs(ctx, ctx.state, renderType, enabled, func)

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(ctx: CombinedContext, state: BlockState, renderType: BlockRenderType, enabled: Boolean = true, func: ()->Unit) {
        if (isAvailable && enabled) {
            val buffer = ctx.renderCtx.renderBuffer
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
    inline fun grass(ctx: CombinedContext, enabled: Boolean = true, func: ()->Unit) =
        renderAs(ctx, defaultGrass, MODEL, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(ctx: CombinedContext, enabled: Boolean = true, func: ()->Unit) =
        renderAs(ctx, defaultLeaves, MODEL, enabled, func)
}
