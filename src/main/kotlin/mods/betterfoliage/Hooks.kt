@file:JvmName("Hooks")
package mods.betterfoliage

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.render.EntityFallingLeavesFX
import mods.betterfoliage.render.EntityRisingSoulFX
import mods.betterfoliage.render.block.vanillaold.LogRegistry
import mods.betterfoliage.render.canRenderInLayer
import mods.betterfoliage.render.down1
import mods.betterfoliage.render.isCutout
import mods.betterfoliage.render.up1
import mods.betterfoliage.render.old.BasicBlockCtx
import mods.betterfoliage.render.old.CachedBlockCtx
import mods.betterfoliage.render.old.NonNullWorld
import mods.betterfoliage.render.old.RenderCtx
import mods.betterfoliage.render.lighting.DefaultLightingCtx
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.util.ThreadLocalDelegate
import mods.betterfoliage.util.plus
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.ILightReader
import net.minecraft.world.World
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

fun getAmbientOcclusionLightValueOverride(original: Float, state: BlockState): Float {
    if (Config.enabled && Config.roundLogs.enabled && BlockConfig.logBlocks.matchesClass(state.block)) return Config.roundLogs.dimming.toFloat();
    return original
}

fun getUseNeighborBrightnessOverride(original: Boolean, state: BlockState): Boolean {
    return original || (Config.enabled && Config.roundLogs.enabled && BlockConfig.logBlocks.matchesClass(state.block));
}

fun onClientBlockChanged(worldClient: ClientWorld, pos: BlockPos, oldState: BlockState, newState: BlockState, flags: Int) {
    ChunkOverlayManager.onBlockChange(worldClient, pos)
}

fun onRandomDisplayTick(block: Block, state: BlockState, world: World, pos: BlockPos, random: Random) {
    if (Config.enabled &&
        Config.risingSoul.enabled &&
        state.block == Blocks.SOUL_SAND &&
        world.isAirBlock(pos + up1) &&
        Math.random() < Config.risingSoul.chance) {
            EntityRisingSoulFX(world, pos).addIfValid()
    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        BlockConfig.leafBlocks.matchesClass(state.block) &&
        world.isAirBlock(pos + down1) &&
        Math.random() < Config.fallingLeaves.chance) {
            EntityFallingLeavesFX(world, pos).addIfValid()
    }
}

fun getVoxelShapeOverride(state: BlockState, reader: IBlockReader, pos: BlockPos, dir: Direction): VoxelShape {
    if (LogRegistry[state, reader, pos] != null) return VoxelShapes.empty()
    return state.getFaceOcclusionShape(reader, pos, dir)
}

val lightingCtx by ThreadLocalDelegate { DefaultLightingCtx(BasicBlockCtx(NonNullWorld, BlockPos.ZERO)) }
fun renderWorldBlock(dispatcher: BlockRendererDispatcher,
                     state: BlockState,
                     pos: BlockPos,
                     reader: ILightReader,
                     matrixStack: MatrixStack,
                     buffer: IVertexBuilder,
                     checkSides: Boolean,
                     random: Random,
                     modelData: IModelData,
                     layer: RenderType
): Boolean {
    // build context
    val blockCtx = CachedBlockCtx(reader, pos)
    val renderCtx = RenderCtx(dispatcher, buffer, matrixStack, layer, checkSides, random, modelData)
    lightingCtx.reset(blockCtx)
    val combinedCtx = CombinedContext(blockCtx, renderCtx, lightingCtx)

    combinedCtx.render()
    return combinedCtx.hasRendered

    // loop render decorators
    val doBaseRender = state.canRenderInLayer(layer) || (layer == targetCutoutLayer && state.canRenderInLayer(
        otherCutoutLayer
    ))
    Client.renderers.forEach { renderer ->
        if (renderer.isEligible(combinedCtx)) {
            // render on the block's default layer
            // also render on the cutout layer if the renderer requires it

            val doCutoutRender = renderer.renderOnCutout && layer == targetCutoutLayer
            val stopRender = renderer.onlyOnCutout && !layer.isCutout

            if ((doBaseRender || doCutoutRender) && !stopRender) {
                renderer.render(combinedCtx)
                return combinedCtx.hasRendered
            }
        }
    }

    // no render decorators have taken on this block, proceed to normal rendering
    combinedCtx.render()
    return combinedCtx.hasRendered
}

fun canRenderInLayerOverride(state: BlockState, layer: RenderType) = state.canRenderInLayer(layer) || layer == targetCutoutLayer

fun canRenderInLayerOverrideOptifine(state: BlockState, optifineReflector: Any?, layerArray: Array<Any>) =
    canRenderInLayerOverride(state, layerArray[0] as RenderType)

val targetCutoutLayer: RenderType get() = if (Minecraft.getInstance().gameSettings.mipmapLevels > 0) RenderType.getCutoutMipped() else RenderType.getCutout()
val otherCutoutLayer: RenderType get() = if (Minecraft.getInstance().gameSettings.mipmapLevels > 0) RenderType.getCutout() else RenderType.getCutoutMipped()
