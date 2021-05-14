@file:JvmName("Hooks")
package mods.betterfoliage

import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.WeightedModelWrapper
import mods.betterfoliage.render.block.vanilla.RoundLogKey
import mods.betterfoliage.render.particle.FallingLeafParticle
import mods.betterfoliage.render.particle.LeafBlockModel
import mods.betterfoliage.render.particle.RisingSoulParticle
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.Minecraft
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import java.util.Random

fun getAmbientOcclusionLightValueOverride(original: Float, state: BlockState): Float {
    if (Config.enabled && Config.roundLogs.enabled && BetterFoliage.blockTypes.hasTyped<RoundLogKey>(state))
        return Config.roundLogs.dimming.toFloat()
    return original
}

fun onClientBlockChanged(worldClient: ClientWorld, pos: BlockPos, oldState: BlockState, newState: BlockState, flags: Int) {
    ChunkOverlayManager.onBlockChange(worldClient, pos)
}

fun onRandomDisplayTick(world: ClientWorld, pos: BlockPos, random: Random) {
    val state = world.getBlockState(pos)
    if (Config.enabled &&
        Config.risingSoul.enabled &&
        state.block == Blocks.SOUL_SAND &&
        world.getBlockState(pos.relative(UP)).isAir &&
        Math.random() < Config.risingSoul.chance) {
            RisingSoulParticle(world, pos).addIfValid()
    }

    if (Config.enabled &&
        Config.fallingLeaves.enabled &&
        random.nextDouble() < Config.fallingLeaves.chance &&
        world.getBlockState(pos.relative(DOWN)).isAir
    ) {
        (getActualRenderModel(world, pos, state, random) as? LeafBlockModel)?.let { leafModel ->
            val blockColor = Minecraft.getInstance().blockColors.getColor(state, world, pos, 0)
            FallingLeafParticle(world, pos, leafModel.key, blockColor, random).addIfValid()
        }
    }
}

fun getVoxelShapeOverride(state: BlockState, reader: IBlockReader, pos: BlockPos, dir: Direction): VoxelShape {
    if (Config.enabled && Config.roundLogs.enabled && BetterFoliage.blockTypes.hasTyped<RoundLogKey>(state))
        return VoxelShapes.empty()
    return state.getFaceOcclusionShape(reader, pos, dir)
}

fun shouldForceSideRenderOF(state: BlockState, world: IBlockReader, pos: BlockPos, face: Direction) =
    world.getBlockState(pos.relative(face)).let { neighbor -> BetterFoliage.blockTypes.hasTyped<RoundLogKey>(neighbor) }

fun getActualRenderModel(world: IBlockDisplayReader, pos: BlockPos, state: BlockState, random: Random): SpecialRenderModel? {
    val model = Minecraft.getInstance().blockRenderer.blockModelShaper.getBlockModel(state) as? SpecialRenderModel
        ?: return null
    if (model is WeightedModelWrapper) {
        random.setSeed(state.getSeed(pos))
        return model.getModel(random).model
    }
    return model
}