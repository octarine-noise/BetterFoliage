@file:JvmName("Hooks")
package mods.betterfoliage

import mods.betterfoliage.config.Config
import mods.betterfoliage.model.getActualRenderModel
import mods.betterfoliage.render.particle.FallingLeafParticle
import mods.betterfoliage.texture.LeafBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import java.util.Random

fun getAmbientOcclusionLightValueOverride(original: Float, state: BlockState): Float {
//    if (Config.enabled && Config.roundLogs.enabled && BlockConfig.logBlocks.matchesClass(state.block)) return Config.roundLogs.dimming.toFloat();
    return original
}

fun getUseNeighborBrightnessOverride(original: Boolean, state: BlockState): Boolean {
//    return original || (Config.enabled && Config.roundLogs.enabled && BlockConfig.logBlocks.matchesClass(state.block));
    return original
}

fun onClientBlockChanged(worldClient: ClientWorld, pos: BlockPos, oldState: BlockState, newState: BlockState, flags: Int) {
//    ChunkOverlayManager.onBlockChange(worldClient, pos)
}

fun onRandomDisplayTick(block: Block, state: BlockState, world: World, pos: BlockPos, random: Random) {
//    if (Config.enabled &&
//        Config.risingSoul.enabled &&
//        state.block == Blocks.SOUL_SAND &&
//        world.isAirBlock(pos + up1) &&
//        Math.random() < Config.risingSoul.chance) {
//            EntityRisingSoulFX(world, pos).addIfValid()
//    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        random.nextDouble() < Config.fallingLeaves.chance &&
        world.isAirBlock(pos.offset(DOWN))
    ) {
        (getActualRenderModel(world, pos, state, random) as? LeafBlockModel)?.let { leafModel ->
            val blockColor = Minecraft.getInstance().blockColors.getColor(state, world, pos, 0)
            FallingLeafParticle(world, pos, leafModel.key, blockColor, random).addIfValid()
        }
    }
}

fun getVoxelShapeOverride(state: BlockState, reader: IBlockReader, pos: BlockPos, dir: Direction): VoxelShape {
//    if (LogRegistry[state, reader, pos] != null) return VoxelShapes.empty()
    return state.getFaceOcclusionShape(reader, pos, dir)
}
