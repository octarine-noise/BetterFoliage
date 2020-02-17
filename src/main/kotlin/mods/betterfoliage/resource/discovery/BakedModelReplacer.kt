package mods.betterfoliage.resource.discovery

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BlockModelsReloadCallback
import mods.betterfoliage.ModelLoadingCallback
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.Invalidator
import mods.betterfoliage.util.YarnHelper
import mods.betterfoliage.util.get
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier

// net.minecraft.client.render.block.BlockModels.models
val BlockModels_models = YarnHelper.requiredField<Map<BlockState, BakedModel>>("net.minecraft.class_773", "field_4162", "Ljava/util/Map;")

class BakedModelReplacer : ModelLoadingCallback, ClientSpriteRegistryCallback, BlockModelsReloadCallback, Invalidator, HasLogger {
    override val logger get() = BetterFoliage.logDetail

    val discoverers = mutableListOf<ModelDiscovery>()
    override val callbacks = mutableListOf<WeakReference<()->Unit>>()

    protected var keys = emptyMap<BlockState, BlockRenderKey>()

    operator fun get(state: BlockState) = keys[state]
    inline fun <reified T> getTyped(state: BlockState) = get(state) as? T

    var currentLoader: ModelLoader? = null

    override fun beginLoadModels(loader: ModelLoader, manager: ResourceManager) {
        // Step 1: get a hold of the ModelLoader instance when model reloading starts
        currentLoader = loader
        log("reloading block discovery configuration")
        BetterFoliage.blockConfig.reloadConfig(manager)
        invalidate()
    }

    override fun registerSprites(atlasTexture: SpriteAtlasTexture, registry: ClientSpriteRegistryCallback.Registry) {
        // Step 2: ModelLoader is finished with the unbaked models by now, we can inspect them
        log("discovering blocks")
        val idSet = Collections.synchronizedSet(mutableSetOf<Identifier>())
        val allKeys = discoverers.map {
            // run model discoverers in parallel
            CompletableFuture.supplyAsync(Supplier {
                it.discover(currentLoader!!, Consumer { idSet.add(it) })
            }, MinecraftClient.getInstance())
        }.map { it.join() }
        idSet.forEach { registry.register(it) }

        val result = mutableMapOf<BlockState, BlockRenderKey>()
        allKeys.forEach { keys ->
            keys.entries.forEach { (state, key) ->
                val oldKey = result[state]
                if (oldKey != null) log("Replacing $oldKey with $key for state $state")
                else log("Adding replacement $key for state $state")
                result[state] = key
            }
        }

        keys = result
    }

    override fun reloadBlockModels(blockModels: BlockModels) {
        // Step 3: replace the baked models with our own
        log("block model baking finished")
        val modelMap = blockModels[BlockModels_models] as MutableMap<BlockState, BakedModel>
        keys.forEach { (state, key) ->
            val oldModel = modelMap[state]
            if (oldModel == null) log("Cannot find model for state $state, ignoring")
            else {
                try {
                    val newModel = key.replace(oldModel, state)
                    modelMap[state] = newModel
                    log("Replaced model for state $state with $key")
                } catch (e: Exception) {
                    log("Error creating model for state $state with $key", e)
                }
            }
        }
    }

    init {
        ModelLoadingCallback.EVENT.register(this)
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(this)
        BlockModelsReloadCallback.EVENT.register(this)
    }
}

