@file:JvmName("RendererHolder")
package mods.octarinecore.client.render

import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.resource.ResourceHandler
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirOffsets
import mods.octarinecore.common.plus
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.color.BlockColors
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraft.world.biome.BiomeGenBase

/**
 * [ThreadLocal] instance of [BlockContext] representing the block being rendered.
 */
val blockContext by ThreadLocalDelegate { BlockContext() }

/**
 * [ThreadLocal] instance of [ModelRenderer].
 */
val modelRenderer by ThreadLocalDelegate { ModelRenderer() }

val blockColors = ThreadLocal<BlockColors>()

abstract class AbstractBlockRenderingHandler(modId: String) : ResourceHandler(modId) {

    open val moveToCutout: Boolean get() = true

    // ============================
    // Custom rendering
    // ============================
    abstract fun isEligible(ctx: BlockContext): Boolean
    abstract fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean

    // ============================
    // Vanilla rendering wrapper
    // ============================
    /**
     * Render the block in the current [BlockContext]
     */
    fun renderWorldBlockBase(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer?): Boolean {
        ctx.blockState(Int3.zero).let {
            if (layer == null || it.block.canRenderInLayer(layer))
                return dispatcher.renderBlock(it, ctx.pos, ctx.world, renderer)
        }
        return false
    }

}

data class BlockData(val state: IBlockState, val color: Int, val brightness: Int)

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
class BlockContext() {
    var world: IBlockAccess? = null
    var pos = BlockPos.ORIGIN

    fun set(world: IBlockAccess, pos: BlockPos) { this.world = world; this.pos = pos; }

    val block: Block get() = block(Int3.zero)
    fun block(offset: Int3) = blockState(offset).block
    fun blockState(offset: Int3) = (pos + offset).let { world!!.getBlockState(it) }
    fun blockData(offset: Int3) = (pos + offset).let { pos ->
        world!!.getBlockState(pos).let { state ->
            BlockData(
                state,
                Minecraft.getMinecraft().blockColors.colorMultiplier(state, world!!, pos, 0),
                state.block.getPackedLightmapCoords(state, world!!, pos)
            )
        }
    }

    /** Get the biome ID at the block position. */
    val biomeId: Int get() = BiomeGenBase.getIdForBiome(world!!.getBiomeGenForCoords(pos))

    /** Get the centerpoint of the block being rendered. */
    val blockCenter: Double3 get() = Double3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)

    val chunkBase: Double3 get() {
        val cX = if (pos.x >= 0) pos.x / 16 else (pos.x + 1) / 16 - 1
        val cY = pos.y / 16
        val cZ = if (pos.z >= 0) pos.z / 16 else (pos.z + 1) / 16 - 1
        return Double3(cX * 16.0, cY * 16.0, cZ * 16.0)
    }

    /** Is the block surrounded by other blocks that satisfy the predicate on all sides? */
    fun isSurroundedBy(predicate: (IBlockState)->Boolean) = forgeDirOffsets.all { predicate(blockState(it)) }

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun random(seed: Int): Int {
        var value = (pos.x * pos.x + pos.y * pos.y + pos.z * pos.z + pos.x * pos.y + pos.y * pos.z + pos.z * pos.x + (seed * seed)) and 63
        value = (3 * pos.x * value + 5 * pos.y * value + 7 * pos.z * value + (11 * seed)) and 63
        return value
    }

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { random(it) }

    /** Get the distance of the block from the camera (player). */
    val cameraDistance: Int get() {
        val camera = Minecraft.getMinecraft().renderViewEntity ?: return 0
        return Math.abs(pos.x - MathHelper.floor_double(camera.posX)) +
               Math.abs(pos.y - MathHelper.floor_double(camera.posY)) +
               Math.abs(pos.z - MathHelper.floor_double(camera.posZ))
    }
}