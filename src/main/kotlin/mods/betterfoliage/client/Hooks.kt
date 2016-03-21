@file:JvmName("Hooks")
@file:SideOnly(Side.CLIENT)
package mods.betterfoliage.client

import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.EntityFallingLeavesFX
import mods.betterfoliage.client.render.EntityRisingSoulFX
import mods.betterfoliage.client.render.down1
import mods.betterfoliage.client.render.up1
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.client.resource.LoadModelDataEvent
import mods.octarinecore.common.plus
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.init.Blocks
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

fun shouldRenderBlockSideOverride(original: Boolean, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
    return original && !(Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(blockAccess.getBlockState(pos).block));
}

fun getAmbientOcclusionLightValueOverride(original: Float, state: IBlockState): Float {
    if (Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(state.block)) return Config.roundLogs.dimming;
    return original;
}

fun getUseNeighborBrightnessOverride(original: Boolean, state: IBlockState): Boolean {
    return original || (Config.enabled && Config.roundLogs.enabled && Config.blocks.logs.matchesID(state.block));
}

fun onRandomDisplayTick(world: World, state: IBlockState, pos: BlockPos) {
    if (Config.enabled &&
        Config.risingSoul.enabled &&
        state.block == Blocks.soul_sand &&
        world.isAirBlock(pos + up1) &&
        Math.random() < Config.risingSoul.chance) {
            EntityRisingSoulFX(world, pos).addIfValid()
    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        Config.blocks.leaves.matchesID(state.block) &&
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
                     worldRenderer: VertexBuffer,
                     layer: BlockRenderLayer
): Boolean {
    val isCutout = layer == CUTOUT_MIPPED || layer == CUTOUT
    val needsCutout = state.block.canRenderInLayer(CUTOUT_MIPPED) || state.block.canRenderInLayer(CUTOUT)
    val canRender = (isCutout && needsCutout) || state.block.canRenderInLayer(layer)


    blockContext.let { ctx ->
        ctx.set(blockAccess, pos)
        Client.renderers.forEach { renderer ->
            if (renderer.isEligible(ctx)) {
                return if (renderer.moveToCutout) {
                    if (isCutout) renderer.render(ctx, dispatcher, worldRenderer, layer) else false
                } else {
                    renderer.render(ctx, dispatcher, worldRenderer, layer)
                }
            }
        }
    }
    return if (canRender) dispatcher.renderBlock(state, pos, blockAccess, worldRenderer) else false
}

fun canRenderBlockInLayer(block: Block, layer: BlockRenderLayer): Boolean {
    if (layer == CUTOUT_MIPPED && !block.canRenderInLayer(CUTOUT)) {
        return true
    }
    return block.canRenderInLayer(layer)

}