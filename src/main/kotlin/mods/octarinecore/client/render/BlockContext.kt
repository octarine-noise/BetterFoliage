package mods.octarinecore.client.render

import mods.octarinecore.common.*
import mods.octarinecore.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import java.util.*

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
interface BlockCtx {
    val world: IEnviromentBlockReader
    val pos: BlockPos

    fun offset(dir: Direction) = offset(dir.offset)
    fun offset(offset: Int3): BlockCtx

    val state: BlockState get() = world.getBlockState(pos)
    fun state(dir: Direction) = world.getBlockState(pos + dir.offset)
    fun state(offset: Int3) = world.getBlockState(pos + offset)

    val biome: Biome get() = world.getBiome(pos)

    val isNormalCube: Boolean get() = state.isNormalCube(world, pos)

    fun shouldSideBeRendered(side: Direction) = Block.shouldSideBeRendered(state, world, pos, side)

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun semiRandom(seed: Int) = semiRandom(pos.x, pos.y, pos.z, seed)

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { semiRandom(it) }
}

open class BasicBlockCtx(
    override val world: IEnviromentBlockReader,
    override val pos: BlockPos
) : BlockCtx {
    override var state: BlockState = world.getBlockState(pos)
        protected set
    override fun offset(offset: Int3) = BasicBlockCtx(world, pos + offset)
    fun cache() = CachedBlockCtx(world, pos)
}

open class CachedBlockCtx(world: IEnviromentBlockReader, pos: BlockPos) : BasicBlockCtx(world, pos) {
    var neighbors = Array<BlockState>(6) { world.getBlockState(pos + allDirections[it].offset) }
    override var biome: Biome = world.getBiome(pos)
    override fun state(dir: Direction) = neighbors[dir.ordinal]
}


data class RenderCtx(
    val dispatcher: BlockRendererDispatcher,
    val renderBuffer: BufferBuilder,
    val layer: BlockRenderLayer,
    val random: Random
) {
    fun render(worldBlock: BlockCtx) = dispatcher.renderBlock(worldBlock.state, worldBlock.pos, worldBlock.world, renderBuffer, random, null)
}

