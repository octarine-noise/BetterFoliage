package mods.betterfoliage.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.lighting.getBufferBuilder
import mods.betterfoliage.util.getAllMethods
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockRenderLayer
import net.minecraft.block.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.BufferBuilder

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration {

    val BufferBuilder_SVertexBuilder = BufferBuilder::class.java.fields.find { it.name == "sVertexBuilder" }
    val SVertexBuilder_pushState = getAllMethods("net.optifine.shaders.SVertexBuilder", "pushEntity").find { it.parameterCount == 1 }
    val SVertexBuilder_popState = getAllMethods("net.optifine.shaders.SVertexBuilder", "popEntity").find { it.parameterCount == 0 }
    val BlockAliases_getAliasBlockId = getAllMethods("net.optifine.shaders.BlockAliases", "getAliasBlockId").firstOrNull()

    @JvmStatic val isAvailable =
        listOf(BufferBuilder_SVertexBuilder).all { it != null } &&
        listOf(SVertexBuilder_pushState, SVertexBuilder_popState, BlockAliases_getAliasBlockId).all { it != null }

    val defaultLeaves = Blocks.OAK_LEAVES.defaultState
    val defaultGrass = Blocks.TALL_GRASS.defaultState

    init {
        BetterFoliage.logger.info("[BetterFoliage] ShadersMod integration is ${if (isAvailable) "enabled" else "disabled" }")
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(ctx: RenderContext, state: BlockState, layer: BlockRenderLayer, enabled: Boolean = true, func: ()->Unit) {
        if (isAvailable && enabled) {
            val sVertexBuilder = BufferBuilder_SVertexBuilder!!.get(ctx.getBufferBuilder(layer))
            val aliasBlockId = BlockAliases_getAliasBlockId!!.invoke(null, state)
            SVertexBuilder_pushState!!.invoke(sVertexBuilder, aliasBlockId)
            func()
            SVertexBuilder_popState!!.invoke(sVertexBuilder)
        } else {
            func()
        }
    }

    /** Quads rendered inside this block will behave as tallgrass blocks in shader programs. */
    inline fun grass(ctx: RenderContext, enabled: Boolean = true, func: ()->Unit) =
        renderAs(ctx, defaultGrass, CUTOUT_MIPPED, enabled, func)

    /** Quads rendered inside this block will behave as leaf blocks in shader programs. */
    inline fun leaves(ctx: RenderContext, enabled: Boolean = true, func: ()->Unit) =
        renderAs(ctx, defaultLeaves, CUTOUT_MIPPED, enabled, func)
}
