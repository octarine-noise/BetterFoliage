package mods.betterfoliage.chunk

import mods.betterfoliage.util.get
import mods.betterfoliage.util.isInstance
import mods.betterfoliage.ChunkCacheOF
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.DimensionType
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.IWorldReader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.*
import kotlin.collections.set

val IBlockDisplayReader.dimType: DimensionType get() = when {
    this is IWorldReader -> dimensionType()
    this is ChunkRenderCache -> level.dimensionType()
    this.isInstance(ChunkCacheOF) -> this[ChunkCacheOF.chunkCache].level.dimensionType()
    else -> throw IllegalArgumentException("DimensionType of world with class ${this::class.qualifiedName} cannot be determined!")
}

/**
 * Represents some form of arbitrary non-persistent data that can be calculated and cached for each block position
 */
abstract class ChunkOverlayLayer<T> {
    val dimData = IdentityHashMap<DimensionType, SparseChunkedMap<T>>()
    abstract fun calculate(ctx: BlockCtx): T
    abstract fun onBlockUpdate(world: IBlockDisplayReader, pos: BlockPos)

    operator fun get(ctx: BlockCtx): T {
        return dimData
            .getOrPut(ctx.world.dimType) { SparseChunkedMap() }
            .getOrPut(ctx.pos) { calculate(ctx) }
    }

    fun remove(world: IBlockDisplayReader, pos: BlockPos) {
        dimData[world.dimType]?.remove(pos)
    }
}

/**
 * Event forwarder for multiple layers of chunk overlay data.
 */
object ChunkOverlayManager {
    init { MinecraftForge.EVENT_BUS.register(this) }
    val layers = mutableListOf<ChunkOverlayLayer<*>>()

    fun onBlockChange(world: ClientWorld, pos: BlockPos) {
        layers.forEach { layer -> layer.onBlockUpdate(world, pos) }
    }

    @SubscribeEvent
    fun handleUnloadWorld(event: WorldEvent.Unload) = (event.world as? ClientWorld)?.let { world ->
        layers.forEach { layer -> layer.dimData.remove(world.dimType) }
    }

    @SubscribeEvent
    fun handleUnloadChunk(event: ChunkEvent.Unload) = (event.world as? ClientWorld)?.let { world ->
        layers.forEach { layer -> layer.dimData[world.dimType]?.removeChunk(event.chunk.pos) }
    }
}

interface DoubleMap<K1, K2, V> {
    val map1: MutableMap<K1, MutableMap<K2, V>>
    fun createMap2(): MutableMap<K2, V>

    fun remove(key1: K1) {
        map1.remove(key1)
    }
    fun remove(key1: K1, key2: K2) {
        map1[key1]?.remove(key2)
    }
    fun contains(key1: K1) = map1.contains(key1)

    fun getOrSet(key1: K1, key2: K2, factory: () -> V) =
        (map1[key1] ?: createMap2().apply { map1[key1] = this }).let { subMap ->
            subMap[key2] ?: factory().apply { subMap[key2] = this }
        }
}

class SparseChunkedMap<V> {
    val map = object : DoubleMap<ChunkPos, BlockPos, V> {
        override val map1 = mutableMapOf<ChunkPos, MutableMap<BlockPos, V>>()
        override fun createMap2() = mutableMapOf<BlockPos, V>()
    }

    fun getOrPut(pos: BlockPos, factory: () -> V) = map.getOrSet(ChunkPos(pos), pos, factory)
    fun remove(pos: BlockPos) = map.remove(ChunkPos(pos), pos)
    fun removeChunk(pos: ChunkPos) = map.map1.remove(pos)
}