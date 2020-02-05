package mods.betterfoliage

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.world.ClientWorld
import net.minecraft.resource.ResourceManager
import net.minecraft.world.chunk.WorldChunk

interface ClientChunkLoadCallback {
    fun loadChunk(chunk: WorldChunk)
    fun unloadChunk(chunk: WorldChunk)

    companion object {
        @JvmField val EVENT: Event<ClientChunkLoadCallback> = EventFactory.createArrayBacked(ClientChunkLoadCallback::class.java) { listeners ->
            object : ClientChunkLoadCallback {
                override fun loadChunk(chunk: WorldChunk) { listeners.forEach { it.loadChunk(chunk) } }
                override fun unloadChunk(chunk: WorldChunk) { listeners.forEach { it.unloadChunk(chunk) } }
            }
        }
    }
}

interface ClientWorldLoadCallback {
    fun loadWorld(world: ClientWorld)

    companion object {
        @JvmField val EVENT : Event<ClientWorldLoadCallback> = EventFactory.createArrayBacked(ClientWorldLoadCallback::class.java) { listeners ->
            object : ClientWorldLoadCallback {
                override fun loadWorld(world: ClientWorld) { listeners.forEach { it.loadWorld(world) } }
            }
        }
    }
}

interface BlockModelsReloadCallback {
    fun reloadBlockModels(blockModels: BlockModels)

    companion object {
        @JvmField val EVENT: Event<BlockModelsReloadCallback> = EventFactory.createArrayBacked(BlockModelsReloadCallback::class.java) { listeners ->
            object : BlockModelsReloadCallback {
                override fun reloadBlockModels(blockModels: BlockModels) {
                    listeners.forEach { it.reloadBlockModels(blockModels) }
                }
            }
        }
    }
}

/**
 * Event fired when the [ModelLoader] first starts loading models.
 *
 * This happens during the constructor, so BEWARE!
 * Try to avoid any interaction until the block texture atlas starts stitching.
 */
interface ModelLoadingCallback {
    fun beginLoadModels(loader: ModelLoader, manager: ResourceManager)

    companion object {
        @JvmField val EVENT: Event<ModelLoadingCallback> = EventFactory.createArrayBacked(ModelLoadingCallback::class.java) { listeners ->
            object : ModelLoadingCallback {
                override fun beginLoadModels(loader: ModelLoader, manager: ResourceManager) {
                    listeners.forEach { it.beginLoadModels(loader, manager) }
                }
            }
        }
    }
}
