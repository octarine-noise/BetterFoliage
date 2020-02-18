package mods.betterfoliage.client.chunk

import mods.betterfoliage.client.Client
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.IWorldEventListener
import net.minecraft.world.World
import net.minecraft.world.chunk.EmptyChunk
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level

/**
 * Represents some form of arbitrary non-persistent data that can be calculated and cached for each block position
 */
interface ChunkOverlayLayer<T> {
    abstract fun calculate(world: IBlockAccess, pos: BlockPos): T
    abstract fun onBlockUpdate(world: IBlockAccess, pos: BlockPos)
}

/**
 * Query, lazy calculation and lifecycle management of multiple layers of chunk overlay data.
 */
object ChunkOverlayManager : IBlockUpdateListener {
    init {
        Client.log(Level.INFO, "Initializing client overlay manager")
        MinecraftForge.EVENT_BUS.register(this)
    }

    val chunkData = mutableMapOf<ChunkPos, ChunkOverlayData>()
    val layers = mutableListOf<ChunkOverlayLayer<*>>()

    /**
     * Get the overlay data for a given layer and position
     *
     * @param layer Overlay layer to query
     * @param world World to use if calculation of overlay value is necessary
     * @param pos Block position
     */
    fun <T> get(layer: ChunkOverlayLayer<T>, world: IBlockAccess, pos: BlockPos): T? {
        val data = chunkData[ChunkPos(pos)] ?: return null
        data.get(layer, pos).let { value ->
            if (value !== ChunkOverlayData.UNCALCULATED) return value
            val newValue = layer.calculate(world, pos)
            data.set(layer, pos, newValue)
            return newValue
        }
    }

    /**
     * Clear the overlay data for a given layer and position
     *
     * @param layer Overlay layer to clear
     * @param pos Block position
     */
    fun <T> clear(layer: ChunkOverlayLayer<T>, pos: BlockPos) {
        chunkData[ChunkPos(pos)]?.clear(layer, pos)
    }

    override fun notifyBlockUpdate(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState, flags: Int) {
        if (chunkData.containsKey(ChunkPos(pos))) layers.forEach { layer -> layer.onBlockUpdate(world, pos) }
    }

    @SubscribeEvent
    fun handleLoadWorld(event: WorldEvent.Load) {
        if (event.world is WorldClient) {
            event.world.addEventListener(this)
        }
    }

    @SubscribeEvent
    fun handleLoadChunk(event: ChunkEvent.Load) {
        if (event.world is WorldClient && event.chunk !is EmptyChunk) {
            chunkData[event.chunk.pos] = ChunkOverlayData(layers)
        }
    }
    @SubscribeEvent
    fun handleUnloadChunk(event: ChunkEvent.Unload) {
        if (event.world is WorldClient) {
            chunkData.remove(event.chunk.pos)
        }
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

/**
 * IWorldEventListener helper subclass
 * No-op for everything except notifyBlockUpdate()
 */
interface IBlockUpdateListener : IWorldEventListener {
    override fun playSoundToAllNearExcept(player: EntityPlayer?, soundIn: SoundEvent, category: SoundCategory, x: Double, y: Double, z: Double, volume: Float, pitch: Float) {}
    override fun onEntityAdded(entityIn: Entity) {}
    override fun broadcastSound(soundID: Int, pos: BlockPos, data: Int) {}
    override fun playEvent(player: EntityPlayer?, type: Int, blockPosIn: BlockPos, data: Int) {}
    override fun onEntityRemoved(entityIn: Entity) {}
    override fun notifyLightSet(pos: BlockPos) {}
    override fun spawnParticle(particleID: Int, ignoreRange: Boolean, xCoord: Double, yCoord: Double, zCoord: Double, xSpeed: Double, ySpeed: Double, zSpeed: Double, vararg parameters: Int) {}
    override fun spawnParticle(id: Int, ignoreRange: Boolean, minimiseParticleLevel: Boolean, x: Double, y: Double, z: Double, xSpeed: Double, ySpeed: Double, zSpeed: Double, vararg parameters: Int) {}
    override fun playRecord(soundIn: SoundEvent?, pos: BlockPos) {}
    override fun sendBlockBreakProgress(breakerId: Int, pos: BlockPos, progress: Int) {}
    override fun markBlockRangeForRenderUpdate(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) {}
}
