package mods.betterfoliage.chunk

import mods.betterfoliage.ChunkRendererRegion_world
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.chunk.ChunkRendererRegion
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import net.minecraft.world.WorldView
import net.minecraft.world.biome.Biome

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
interface BlockCtx {
    val world: BlockRenderView
    val pos: BlockPos

    fun offset(dir: Direction) = offset(dir.offset)
    fun offset(offset: Int3): BlockCtx

    val state: BlockState get() = world.getBlockState(pos)
    fun state(dir: Direction) = world.getBlockState(pos + dir.offset)
    fun state(offset: Int3) = world.getBlockState(pos + offset)

    val biome: Biome? get() =
        (world as? WorldView)?.getBiome(pos) ?:
        (world as? ChunkRendererRegion)?.let { ChunkRendererRegion_world[it]?.getBiome(pos) }

    val isNormalCube: Boolean get() = state.isSimpleFullBlock(world, pos)

    fun shouldSideBeRendered(side: Direction) = Block.shouldDrawSide(state, world, pos, side)

    fun isNeighborSolid(dir: Direction) = offset(dir).let { it.state.isSideSolidFullSquare(it.world, it.pos, dir.opposite) }

    fun model(dir: Direction) = state(dir).let { MinecraftClient.getInstance().blockRenderManager.getModel(it)!! }
    fun model(offset: Int3) = state(offset).let { MinecraftClient.getInstance().blockRenderManager.getModel(it)!! }
}

open class BasicBlockCtx(
    override val world: BlockRenderView,
    override val pos: BlockPos
) : BlockCtx {
    override val state = world.getBlockState(pos)
    override fun offset(offset: Int3) = BasicBlockCtx(world, pos + offset)
    fun cache() = CachedBlockCtx(world, pos)
}

open class CachedBlockCtx(world: BlockRenderView, pos: BlockPos) : BasicBlockCtx(world, pos) {
    var neighbors = Array<BlockState>(6) { world.getBlockState(pos + allDirections[it].offset) }
    override var biome: Biome? = super.biome
    override fun state(dir: Direction) = neighbors[dir.ordinal]
}
