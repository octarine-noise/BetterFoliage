package mods.betterfoliage.chunk

import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import mods.betterfoliage.util.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.biome.Biome
import net.minecraft.world.level.ColorResolver

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
interface BlockCtx {
    val world: ILightReader
    val pos: BlockPos

    fun offset(dir: Direction) = offset(dir.offset)
    fun offset(offset: Int3): BlockCtx

    val state: BlockState get() = world.getBlockState(pos)
    fun state(offset: Int3) = world.getBlockState(pos + offset)
    fun state(dir: Direction) = state(dir.offset)

    fun isAir(offset: Int3) = (pos + offset).let { world.getBlockState(it).isAir(world, it) }
    fun isAir(dir: Direction) = isAir(dir.offset)

    val biome: Biome? get() =
        (world as? IWorldReader)?.getBiome(pos) ?:
        (world as? ChunkRenderCache)?.world?.getBiome(pos)

    val isNormalCube: Boolean get() = state.isNormalCube(world, pos)

    fun isNeighborSolid(dir: Direction) = offset(dir).let { it.state.isSolidSide(it.world, it.pos, dir.opposite) }

    fun shouldSideBeRendered(side: Direction) = Block.shouldSideBeRendered(state, world, pos, side)

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun semiRandom(seed: Int) = pos.semiRandom(seed)

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { semiRandom(it) }

    fun color(resolver: ColorResolver) = world.getBlockColor(pos, resolver)
}

class BasicBlockCtx(
    override val world: ILightReader,
    override val pos: BlockPos
) : BlockCtx {
    override val state: BlockState = world.getBlockState(pos)
    override fun offset(offset: Int3) = BasicBlockCtx(world, pos + offset)
}
