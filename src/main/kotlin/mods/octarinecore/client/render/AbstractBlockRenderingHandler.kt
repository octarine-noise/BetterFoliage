@file:JvmName("RendererHolder")
package mods.octarinecore.client.render

import mods.betterfoliage.client.render.canRenderInCutout
import mods.betterfoliage.client.render.isCutout
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.resource.ResourceHandler
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirOffsets
import mods.octarinecore.common.plus
import mods.octarinecore.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.color.BlockColors
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.biome.Biome
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.eventbus.api.IEventBus
import java.util.*
import kotlin.math.abs

/**
 * [ThreadLocal] instance of [BlockContext] representing the block being rendered.
 */
val blockContext by ThreadLocalDelegate { BlockContext() }

/**
 * [ThreadLocal] instance of [ModelRenderer].
 */
val modelRenderer by ThreadLocalDelegate { ModelRenderer() }

val blockColors = ThreadLocal<BlockColors>()

abstract class AbstractBlockRenderingHandler(modId: String, modBus: IEventBus) : ResourceHandler(modId, modBus) {

    open val addToCutout: Boolean get() = true

    // ============================
    // Custom rendering
    // ============================
    abstract fun isEligible(ctx: BlockContext): Boolean
    abstract fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean

    // ============================
    // Vanilla rendering wrapper
    // ============================
    /**
     * Render the block in the current [BlockContext]
     */
    fun renderWorldBlockBase(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer?): Boolean {
        ctx.blockState(Int3.zero).let { state ->
            if (layer == null ||
                state.canRenderInLayer(layer) ||
                (state.canRenderInCutout() && layer.isCutout)) {
                return dispatcher.renderBlock(state, ctx.pos, ctx.reader!!, renderer, random, modelData)
            }
        }
        return false
    }

}

data class BlockData(val state: BlockState, val color: Int, val brightness: Int)

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
open class BlockContext(
    var reader: IEnviromentBlockReader? = null,
    open var pos: BlockPos = BlockPos.ZERO
)  {
    fun set(blockReader: IEnviromentBlockReader, pos: BlockPos) { this.reader = blockReader; this.pos = pos; }

    val block: Block get() = block(Int3.zero)
    fun block(offset: Int3) = blockState(offset).block
    fun blockState(offset: Int3) = (pos + offset).let { reader!!.getBlockState(it) }
    fun isNormalCube(offset: Int3) = (pos + offset).let { reader!!.getBlockState(it).isNormalCube(reader, it) }

    /** Get the centerpoint of the block being rendered. */
    val blockCenter: Double3 get() = Double3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    val chunkBase: Double3 get() {
        val cX = if (pos.x >= 0) pos.x / 16 else (pos.x + 1) / 16 - 1
        val cY = pos.y / 16
        val cZ = if (pos.z >= 0) pos.z / 16 else (pos.z + 1) / 16 - 1
        return Double3(cX * 16.0, cY * 16.0, cZ * 16.0)
    }

    /** Get the biome at the block position. */
    val biome: Biome get() = reader!!.getBiome(pos)

    fun blockData(offset: Int3) = (pos + offset).let { pos ->
        reader!!.getBlockState(pos).let { state ->
            BlockData(
                state,
                Minecraft.getInstance().blockColors.getColor(state, reader!!, pos, 0),
                state.getPackedLightmapCoords(reader!!, pos)
            )
        }
    }

    fun shouldSideBeRendered(dir: Direction) = Block.shouldSideBeRendered(blockState(Int3.zero), reader, pos, dir)

    /** Is the block surrounded by other blocks that satisfy the predicate on all sides? */
    fun isSurroundedBy(predicate: (BlockState)->Boolean) = forgeDirOffsets.all { predicate(blockState(it)) }
    val isSurroundedByNormal: Boolean get() = forgeDirOffsets.all { isNormalCube(it) }

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun random(seed: Int) = semiRandom(pos.x, pos.y, pos.z, seed)

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { random(it) }

    /** Get the distance of the block from the camera (player). */
    val cameraDistance: Int get() {
        val camera = Minecraft.getInstance().renderViewEntity ?: return 0
        return abs(pos.x - MathHelper.floor(camera.posX)) +
            abs(pos.y - MathHelper.floor(camera.posY)) +
            abs(pos.z - MathHelper.floor(camera.posZ))
    }
}
