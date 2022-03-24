package mods.betterfoliage.chunk

import mods.betterfoliage.*
import mods.betterfoliage.util.YarnHelper
import mods.betterfoliage.util.get
import net.minecraft.client.render.chunk.ChunkRendererRegion
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.BlockRenderView
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.chunk.WorldChunk
import net.minecraft.world.dimension.DimensionType
import java.util.*
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.associateWith
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

val BlockRenderView.dimType: DimensionType? get() = when {
    this is WorldView -> dimension
    this is ChunkRendererRegion -> this[ChunkRendererRegion_world]!!.dimension
//    this.isInstance(ChunkCacheOF) -> this[ChunkCacheOF.chunkCache]!!.dimType
    else -> null
}

/**
 * Represents some form of arbitrary non-persistent data that can be calculated and cached for each block position
 */
interface ChunkOverlayLayer<T> {
    fun calculate(ctx: BlockCtx): T
    fun onBlockUpdate(world: WorldView, pos: BlockPos)
}

/**
 * Query, lazy calculation and lifecycle management of multiple layers of chunk overlay data.
 */
object ChunkOverlayManager : ClientChunkLoadCallback, ClientWorldLoadCallback {

    init {
        ClientWorldLoadCallback.EVENT.register(this)
        ClientChunkLoadCallback.EVENT.register(this)
    }

    val chunkData = IdentityHashMap<DimensionType, MutableMap<ChunkPos, ChunkOverlayData>>()
    val layers = mutableListOf<ChunkOverlayLayer<*>>()

    /**
     * Get the overlay data for a given layer and position
     *
     * @param layer Overlay layer to query
     * @param reader World to use if calculation of overlay value is necessary
     * @param pos Block position
     */
    fun <T> get(layer: ChunkOverlayLayer<T>, ctx: BlockCtx): T? {
        val data = chunkData[ctx.world.dimType]?.get(ChunkPos(ctx.pos)) ?: return null
        data.get(layer, ctx.pos).let { value ->
            if (value !== ChunkOverlayData.UNCALCULATED) return value
            val newValue = layer.calculate(ctx)
            data.set(layer, ctx.pos, newValue)
            return newValue
        }
    }

    /**
     * Clear the overlay data for a given layer and position
     *
     * @param layer Overlay layer to clear
     * @param pos Block position
     */
    fun <T> clear(dimension: DimensionType, layer: ChunkOverlayLayer<T>, pos: BlockPos) {
        chunkData[dimension]?.get(ChunkPos(pos))?.clear(layer, pos)
    }

    fun onBlockChange(world: ClientWorld, pos: BlockPos) {
        if (chunkData[world.dimType]?.containsKey(ChunkPos(pos)) == true) {
            layers.forEach { layer -> layer.onBlockUpdate(world, pos) }
        }
    }

    override fun loadChunk(chunk: WorldChunk) {
        chunk[WorldChunk_world]!!.dimType?.let { dim ->
            val data = chunkData[dim] ?: mutableMapOf<ChunkPos, ChunkOverlayData>().apply { chunkData[dim] = this }
            data.let { chunks ->
                // check for existence first because Optifine fires a TON of these
                if (chunk.pos !in chunks.keys) chunks[chunk.pos] = ChunkOverlayData(layers)
            }
        }
    }

    override fun unloadChunk(chunk: WorldChunk) {
        chunk[WorldChunk_world]!!.dimType?.let { dim ->
            chunkData[dim]?.remove(chunk.pos)
        }
    }

    override fun loadWorld(world: ClientWorld) {
        val dim = world.dimType
//        chunkData.keys.forEach { if (it == dim) chunkData[dim] = mutableMapOf() else chunkData.remove(dim)}
    }
}

class ChunkOverlayData(layers: List<ChunkOverlayLayer<*>>) {
    val BlockPos.isValid: Boolean get() = y in validYRange
    val rawData = layers.associateWith { emptyOverlay() }
    fun <T> get(layer: ChunkOverlayLayer<T>, pos: BlockPos): T? = if (pos.isValid) rawData[layer]?.get(pos.x and 15)?.get(pos.z and 15)?.get(pos.y) as T? else null
    fun <T> set(layer: ChunkOverlayLayer<T>, pos: BlockPos, data: T) = if (pos.isValid) rawData[layer]?.get(pos.x and 15)?.get(pos.z and 15)?.set(pos.y, data) else null
    fun <T> clear(layer: ChunkOverlayLayer<T>, pos: BlockPos) = if (pos.isValid) rawData[layer]?.get(pos.x and 15)?.get(pos.z and 15)?.set(pos.y, UNCALCULATED) else null

    companion object {
        val UNCALCULATED = object {}
        fun emptyOverlay() = Array(16) { Array(16) { Array<Any?>(256) { UNCALCULATED }}}
        val validYRange = 0 until 256
    }
}
