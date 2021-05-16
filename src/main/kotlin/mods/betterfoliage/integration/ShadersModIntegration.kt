package mods.betterfoliage.integration

import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.allAvailable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.mapArray
import mods.octarinecore.*
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.client.model.pipeline.LightUtil
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for ShadersMod.
 */
object ShadersModIntegration : HasLogger() {
    @JvmStatic val isEffectsAvailable = allAvailable(SVertexBuilder.pushState, SVertexBuilder.popState, BlockAliases.getAliasBlockId)
    @JvmStatic val isDiffuseAvailable = allAvailable(Shaders.shaderPackLoaded, Shaders.blockLightLevel05, Shaders.blockLightLevel06, Shaders.blockLightLevel08)

    @JvmStatic val defaultLeaves = Blocks.OAK_LEAVES.defaultState!!
    @JvmStatic val defaultGrass = Blocks.GRASS.defaultState!!

    @JvmStatic var diffuseShades = Direction.values().mapArray { LightUtil.diffuseLight(it) }

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
        logger.log(INFO, "ShadersMod diffuse shading integration is ${if (isDiffuseAvailable) "enabled" else "disabled" }")
        logger.log(INFO, "ShadersMod vertex shader integration is ${if (isEffectsAvailable) "enabled" else "disabled" }")

        // Recalculate the diffsuse shading values used when resources are reloaded
        if (isDiffuseAvailable) BakeWrapperManager.onInvalidate {
            if (Shaders.shaderPackLoaded.getStatic()) {
                diffuseShades = Direction.values().mapArray { face ->
                    when(face) {
                        DOWN -> Shaders.blockLightLevel05.getStatic()
                        WEST, EAST -> Shaders.blockLightLevel06.getStatic()
                        NORTH, SOUTH -> Shaders.blockLightLevel08.getStatic()
                        else -> LightUtil.diffuseLight(face)
                    }
                }
            } else {
                diffuseShades = Direction.values().mapArray { LightUtil.diffuseLight(it) }
            }
        }
    }

    /** Quads rendered inside this block will use the given block entity data in shader programs. */
    inline fun renderAs(buffer: BufferBuilder, state: BlockState, renderType: BlockRenderType, enabled: Boolean = true, func: ()->Unit) {
        if (isEffectsAvailable && enabled) {
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
