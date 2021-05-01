package mods.betterfoliage.render.old

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import mods.betterfoliage.util.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.biome.Biome
import net.minecraftforge.client.model.data.IModelData
import java.util.*

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
    fun state(dir: Direction) = world.getBlockState(pos + dir.offset)
    fun state(offset: Int3) = world.getBlockState(pos + offset)

    val biome: Biome? get() = (world as? IWorldReader)?.getBiome(pos)

    val isNormalCube: Boolean get() = state.isNormalCube(world, pos)

    fun shouldSideBeRendered(side: Direction) = Block.shouldSideBeRendered(state, world, pos, side)

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun semiRandom(seed: Int) = pos.semiRandom(seed)

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { semiRandom(it) }
}

open class BasicBlockCtx(
    override val world: ILightReader,
    override val pos: BlockPos
) : BlockCtx {
    override var state: BlockState = world.getBlockState(pos)
        protected set
    override fun offset(offset: Int3) = BasicBlockCtx(world, pos + offset)
    fun cache() = CachedBlockCtx(world, pos)
}

open class CachedBlockCtx(world: ILightReader, pos: BlockPos) : BasicBlockCtx(world, pos) {
    var neighbors = Array<BlockState>(6) { world.getBlockState(pos + allDirections[it].offset) }
    override var biome: Biome? = super.biome
    override fun state(dir: Direction) = neighbors[dir.ordinal]
}


data class RenderCtx(
    val dispatcher: BlockRendererDispatcher,
    val renderBuffer: IVertexBuilder,
    val matrixStack: MatrixStack,
    val layer: RenderType,
    val checkSides: Boolean,
    val random: Random,
    val modelData: IModelData
) {
    fun render(worldBlock: BlockCtx) =
//        dispatcher.renderBlock(worldBlock.state, worldBlock.pos, worldBlock.world, renderBuffer, random, modelData)
    dispatcher.renderModel(worldBlock.state, worldBlock.pos, worldBlock.world, matrixStack, renderBuffer, checkSides, random, modelData)
}

