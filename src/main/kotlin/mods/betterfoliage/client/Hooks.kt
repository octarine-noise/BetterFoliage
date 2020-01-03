@file:JvmName("Hooks")
package mods.betterfoliage.client

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.*
import mods.betterfoliage.loader.Refs
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.render.*
import mods.octarinecore.client.render.lighting.DefaultLightingCtx
import mods.octarinecore.client.render.lighting.LightingCtx
import mods.octarinecore.client.resource.LoadModelDataEvent
import mods.octarinecore.common.plus
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.World
import net.minecraftforge.client.model.data.IModelData
import java.util.*

var isAfterPostInit = false
val isOptifinePresent = allAvailable(Refs.OptifineClassTransformer)

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

fun onLoadModelDefinitions(bakery: Any) {
    BetterFoliage.modBus.post(LoadModelDataEvent(bakery as ModelBakery))
}

fun getVoxelShapeOverride(state: BlockState, reader: IBlockReader, pos: BlockPos, dir: Direction): VoxelShape {
    if (LogRegistry[state, reader, pos] != null) return VoxelShapes.empty()
    return state.func_215702_a(reader, pos, dir)
}

val lightingCtx by ThreadLocalDelegate { DefaultLightingCtx(BasicBlockCtx(NonNullWorld, BlockPos.ZERO)) }
fun renderWorldBlock(dispatcher: BlockRendererDispatcher,
                     state: BlockState,
                     pos: BlockPos,
                     reader: IEnviromentBlockReader,
                     buffer: BufferBuilder,
                     random: Random,
                     modelData: IModelData,
                     layer: BlockRenderLayer
): Boolean {
    // build context
    val blockCtx = CachedBlockCtx(reader, pos)
    val renderCtx = RenderCtx(dispatcher, buffer, layer, random)
    lightingCtx.reset(blockCtx)
    val combinedCtx = CombinedContext(blockCtx, renderCtx, lightingCtx)

    // loop render decorators
    val doBaseRender = state.canRenderInLayer(layer) || (layer == targetCutoutLayer && state.canRenderInLayer(otherCutoutLayer))
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

fun canRenderInLayerOverride(state: BlockState, layer: BlockRenderLayer) = state.canRenderInLayer(layer) || layer == targetCutoutLayer

fun canRenderInLayerOverrideOptifine(state: BlockState, optifineReflector: Any?, layerArray: Array<Any>) =
    canRenderInLayerOverride(state, layerArray[0] as BlockRenderLayer)

val targetCutoutLayer: BlockRenderLayer get() = if (Minecraft.getInstance().gameSettings.mipmapLevels > 0) CUTOUT_MIPPED else CUTOUT
val otherCutoutLayer: BlockRenderLayer get() = if (Minecraft.getInstance().gameSettings.mipmapLevels > 0) CUTOUT else CUTOUT_MIPPED
