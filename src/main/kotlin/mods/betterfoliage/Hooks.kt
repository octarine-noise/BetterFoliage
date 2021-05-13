@file:JvmName("Hooks")
package mods.betterfoliage

import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.render.block.vanilla.LeafParticleKey
import mods.betterfoliage.render.block.vanilla.RoundLogKey
import mods.betterfoliage.render.particle.FallingLeafParticle
import mods.betterfoliage.render.particle.RisingSoulParticle
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import mods.betterfoliage.util.random
import mods.betterfoliage.util.randomD
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

fun getAmbientOcclusionLightValueOverride(original: Float, state: BlockState): Float {
    if (BetterFoliage.config.enabled &&
        BetterFoliage.config.roundLogs.enabled &&
        BetterFoliage.blockTypes.hasTyped<RoundLogKey>(state)
    ) return BetterFoliage.config.roundLogs.dimming.toFloat()
    return original
}

fun getUseNeighborBrightnessOverride(original: Boolean, state: BlockState): Boolean {
    return original || (BetterFoliage.config.enabled && BetterFoliage.config.roundLogs.enabled && BetterFoliage.blockConfig.logBlocks.matchesClass(state.block));
}

fun onClientBlockChanged(worldClient: ClientWorld, pos: BlockPos, oldState: BlockState, newState: BlockState) {
    ChunkOverlayManager.onBlockChange(worldClient, pos)
}

fun onRandomDisplayTick(world: ClientWorld, pos: BlockPos) {
    val state = world.getBlockState(pos)

    if (BetterFoliage.config.enabled &&
        BetterFoliage.config.risingSoul.enabled &&
        state.block == Blocks.SOUL_SAND &&
        world.isAir(pos + Direction.UP.offset) &&
        Math.random() < BetterFoliage.config.risingSoul.chance) {
            RisingSoulParticle(world, pos).addIfValid()
    }

    if (BetterFoliage.config.enabled &&
        BetterFoliage.config.fallingLeaves.enabled &&
        world.isAir(pos + Direction.DOWN.offset) &&
        randomD() < BetterFoliage.config.fallingLeaves.chance) {
            BetterFoliage.blockTypes.getTyped<LeafParticleKey>(state)?.let { key ->
                val blockColor = MinecraftClient.getInstance().blockColorMap.getColor(state, world, pos, 0)
                FallingLeafParticle(world, pos, key, blockColor, random).addIfValid()
            }
    }
}

fun getVoxelShapeOverride(state: BlockState, reader: BlockView, pos: BlockPos, dir: Direction): VoxelShape {
    if (BetterFoliage.blockTypes.hasTyped<RoundLogKey>(state)) {
        return VoxelShapes.empty()
    }
    // TODO ?
    return state.getCullingFace(reader, pos, dir)
}
