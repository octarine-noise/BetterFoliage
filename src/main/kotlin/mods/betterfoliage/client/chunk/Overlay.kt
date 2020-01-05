package mods.betterfoliage.client.chunk

import mods.octarinecore.ChunkCacheOF
import mods.octarinecore.client.render.BlockCtx
import mods.octarinecore.metaprog.get
import mods.octarinecore.metaprog.isInstance
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.dimension.DimensionType
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import kotlin.collections.List
import kotlin.collections.MutableMap
import kotlin.collections.associateWith
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

val IEnviromentBlockReader.dimType: DimensionType get() = when {
    this is IWorldReader -> dimension.type
    this is ChunkRenderCache -> world.dimension.type
    this.isInstance(ChunkCacheOF) -> this[ChunkCacheOF.chunkCache].world.dimension.type
    else -> throw IllegalArgumentException("DimensionType of world with class ${this::class.qualifiedName} cannot be determined!")
}

/**
 * Represents some form of arbitrary non-persistent data that can be calculated and cached for each block position
 */
interface ChunkOverlayLayer<T> {
    fun calculate(ctx: BlockCtx): T
    fun onBlockUpdate(world: IEnviromentBlockReader, pos: BlockPos)
}

/**
 * Query, lazy calculation and lifecycle management of multiple layers of chunk overlay data.
 */
object ChunkOverlayManager {

    var tempCounter = 0

    init {
        MinecraftForge.EVENT_BUS.register(this)
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

    @SubscribeEvent
    fun handleLoadWorld(event: WorldEvent.Load) = (event.world as? ClientWorld)?.let { world ->
        chunkData[world.dimType] = mutableMapOf()
    }

    @SubscribeEvent
    fun handleUnloadWorld(event: WorldEvent.Unload) = (event.world as? ClientWorld)?.let { world ->
        chunkData.remove(world.dimType)
    }

    @SubscribeEvent
    fun handleLoadChunk(event: ChunkEvent.Load) = (event.world as? ClientWorld)?.let { world ->
        chunkData[world.dimType]?.let { chunks ->
            // check for existence first because Optifine fires a TON of these
            if (event.chunk.pos !in chunks.keys) chunks[event.chunk.pos] = ChunkOverlayData(layers)
        }
    }

    @SubscribeEvent
    fun handleUnloadChunk(event: ChunkEvent.Unload) = (event.world as? ClientWorld)?.let { world ->
        chunkData[world.dimType]?.remove(event.chunk.pos)
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
