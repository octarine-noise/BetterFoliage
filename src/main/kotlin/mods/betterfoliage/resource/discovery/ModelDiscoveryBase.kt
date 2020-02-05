package mods.betterfoliage.resource.discovery

import mods.betterfoliage.util.HasLogger
import net.minecraft.block.BlockState
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelVariant
import net.minecraft.client.render.model.json.WeightedUnbakedModel
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Consumer

typealias RenderKeyFactory = (SpriteAtlasTexture)->BlockRenderKey

interface BlockRenderKey {
    fun replace(model: BakedModel, state: BlockState): BakedModel = model
}

fun ModelLoader.iterateModels(func: (ModelDiscoveryContext)->Unit) {
    Registry.BLOCK.flatMap { block ->
        block.stateFactory.states.map { state -> state to BlockModels.getModelId(state) }
    }.forEach { (state, stateModelResource) ->
        func(ModelDiscoveryContext(this, state, stateModelResource))
    }
}

/**
 * Information about a single [BlockState] and all the [UnbakedModel]s it could render as.
 */
class ModelDiscoveryContext(
    loader: ModelLoader,
    val state: BlockState,
    val modelId: ModelIdentifier
) {
    val models = loader.unwrapVariants(loader.getOrLoadModel(modelId) to modelId)
        .filter { it.second != loader.getOrLoadModel(ModelLoader.MISSING) }

    fun ModelLoader.unwrapVariants(modelAndLoc: Pair<UnbakedModel, Identifier>): List<Pair<UnbakedModel, Identifier>> = when(val model = modelAndLoc.first) {
        is WeightedUnbakedModel -> (model.variants as List<ModelVariant>).flatMap {
            variant -> unwrapVariants(getOrLoadModel(variant.location) to variant.location)
        }
        is JsonUnbakedModel -> listOf(modelAndLoc)
        else -> emptyList()
    }
}

interface ModelDiscovery {
    fun discover(loader: ModelLoader, atlas: Consumer<Identifier>): Map<BlockState, BlockRenderKey>
}

abstract class ModelDiscoveryBase : ModelDiscovery, HasLogger {
    override fun discover(loader: ModelLoader, atlas: Consumer<Identifier>): Map<BlockState, BlockRenderKey> {
        val keys = mutableMapOf<BlockState, BlockRenderKey>()
        var errors = 0

        loader.iterateModels { ctx ->
            try {
                val result = processModel(ctx, atlas)
                result?.let { keys[ctx.state] = it }
            } catch (e: Exception) {
                errors++
            }
        }
        log("${keys.size} BlockStates discovered, $errors errors")
        return keys
    }

    abstract fun processModel(ctx: ModelDiscoveryContext, atlas: Consumer<Identifier>): BlockRenderKey?
}


