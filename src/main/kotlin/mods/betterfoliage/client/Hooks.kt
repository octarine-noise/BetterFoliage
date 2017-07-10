@file:JvmName("Hooks")
@file:SideOnly(Side.CLIENT)
package mods.betterfoliage.client

import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.*
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.client.resource.LoadModelDataEvent
import mods.octarinecore.common.plus
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

var isAfterPostInit = false
val isOptifinePresent = allAvailable(Refs.OptifineClassTransformer)

fun doesSideBlockRenderingOverride(original: Boolean, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
    return original && !(Config.enabled && Config.roundLogs.enabled && Config.blocks.logClasses.matchesClass(blockAccess.getBlockState(pos).block));
}

fun isOpaqueCubeOverride(original: Boolean, state: IBlockState): Boolean {
    // caution: blocks are initialized and the method called during startup
    if (!isAfterPostInit) return original
    return original && !(Config.enabled && Config.roundLogs.enabled && Config.blocks.logClasses.matchesClass(state.block))
}

fun getAmbientOcclusionLightValueOverride(original: Float, state: IBlockState): Float {
    if (Config.enabled && Config.roundLogs.enabled && Config.blocks.logClasses.matchesClass(state.block)) return Config.roundLogs.dimming;
    return original;
}

fun getUseNeighborBrightnessOverride(original: Boolean, state: IBlockState): Boolean {
    return original || (Config.enabled && Config.roundLogs.enabled && Config.blocks.logClasses.matchesClass(state.block));
}

fun onRandomDisplayTick(world: World, state: IBlockState, pos: BlockPos) {
    if (Config.enabled &&
        Config.risingSoul.enabled &&
        state.block == Blocks.SOUL_SAND &&
        world.isAirBlock(pos + up1) &&
        Math.random() < Config.risingSoul.chance) {
            EntityRisingSoulFX(world, pos).addIfValid()
    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        Config.blocks.leavesClasses.matchesClass(state.block) &&
        world.isAirBlock(pos + down1) &&
        Math.random() < Config.fallingLeaves.chance) {
            EntityFallingLeavesFX(world, pos).addIfValid()
    }
}

fun onAfterLoadModelDefinitions(loader: ModelLoader) {
    MinecraftForge.EVENT_BUS.post(LoadModelDataEvent(loader))
}

fun renderWorldBlock(dispatcher: BlockRendererDispatcher,
                     state: IBlockState,
                     pos: BlockPos,
                     blockAccess: IBlockAccess,
                     worldRenderer: BufferBuilder,
                     layer: BlockRenderLayer
): Boolean {
    val doBaseRender = state.canRenderInLayer(layer) || (layer == targetCutoutLayer && state.canRenderInLayer(otherCutoutLayer))
    blockContext.let { ctx ->
        ctx.set(blockAccess, pos)
        Client.renderers.forEach { renderer ->
            if (renderer.isEligible(ctx)) {
                // render on the block's default layer
                // also render on the cutout layer if the renderer requires it
                if (doBaseRender || (renderer.addToCutout && layer == targetCutoutLayer)) {
                    return renderer.render(ctx, dispatcher, worldRenderer, layer)
                }
            }
        }
    }

    return if (doBaseRender) dispatcher.renderBlock(state, pos, blockAccess, worldRenderer) else false
}

fun canRenderBlockInLayer(block: Block, state: IBlockState, layer: BlockRenderLayer) = block.canRenderInLayer(state, layer) || layer == targetCutoutLayer

val targetCutoutLayer: BlockRenderLayer get() = if (Minecraft.getMinecraft().gameSettings.mipmapLevels > 0) CUTOUT_MIPPED else CUTOUT
val otherCutoutLayer: BlockRenderLayer get() = if (Minecraft.getMinecraft().gameSettings.mipmapLevels > 0) CUTOUT else CUTOUT_MIPPED
