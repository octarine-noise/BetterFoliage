package mods.octarinecore.client.resource

import com.google.common.base.Joiner
import mods.octarinecore.HasLogger
import mods.octarinecore.client.render.BlockCtx
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.common.plus
import mods.octarinecore.common.sinkAsync
import mods.octarinecore.findFirst
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelShapes
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.registries.ForgeRegistries
import java.util.concurrent.CompletableFuture

interface ModelRenderRegistry<T> {
    operator fun get(ctx: BlockCtx) = get(ctx.state, ctx.world, ctx.pos)
    operator fun get(ctx: BlockCtx, offset: Int3) = get(ctx.state(offset), ctx.world, ctx.pos + offset)
    operator fun get(state: BlockState, world: IBlockReader, pos: BlockPos): T?
}

abstract class ModelRenderRegistryRoot<T> : ModelRenderRegistry<T> {
    val registries = mutableListOf<ModelRenderRegistry<T>>()
    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos) = registries.findFirst { it[state, world, pos] }
}

/**
 * Information about a single BlockState and all the IUnbakedModel it could render as.
 */
class ModelDiscoveryContext(
    bakery: ModelBakery,
    val state: BlockState,
    val modelId: ModelResourceLocation
) {
    val models = bakery.unwrapVariants(bakery.getUnbakedModel(modelId) to modelId)
        .filter { it.second != bakery.getUnbakedModel(ModelBakery.MODEL_MISSING) }

    fun ModelBakery.unwrapVariants(modelAndLoc: Pair<IUnbakedModel, ResourceLocation>): List<Pair<IUnbakedModel, ResourceLocation>> = when(val model = modelAndLoc.first) {
        is VariantList -> model.variantList.flatMap { variant -> unwrapVariants(getUnbakedModel(variant.modelLocation) to variant.modelLocation) }
        is BlockModel -> listOf(modelAndLoc)
        else -> emptyList()
    }
}

abstract class ModelDiscovery<T> : HasLogger, AsyncSpriteProvider<ModelBakery>, ModelRenderRegistry<T> {

    var modelData: Map<BlockState, T> = emptyMap()
        protected set

    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos) = modelData[state]

    abstract fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<T>?

    override fun setup(manager: IResourceManager, bakeryF: CompletableFuture<ModelBakery>, atlas: AtlasFuture): StitchPhases {
        val modelDataTemp = mutableMapOf<BlockState, CompletableFuture<T>>()

        return StitchPhases(
            discovery = bakeryF.sinkAsync { bakery ->
                var errors = 0
                bakery.iterateModels { ctx ->
                    try {
                        processModel(ctx, atlas)?.let { modelDataTemp[ctx.state] = it }
                    } catch (e: Exception) {
                        errors++
                    }
                }
                log("${modelDataTemp.size} BlockStates discovered, $errors errors")
            },
            cleanup = atlas.runAfter {
                modelData = modelDataTemp.filterValues { !it.isCompletedExceptionally }.mapValues { it.value.get() }
                val errors = modelDataTemp.values.filter { it.isCompletedExceptionally }.size
                log("${modelData.size} BlockStates loaded, $errors errors")
            }
        )
    }

    fun ModelBakery.iterateModels(func: (ModelDiscoveryContext)->Unit) {
        ForgeRegistries.BLOCKS.flatMap { block ->
            block.stateContainer.validStates.map { state -> state to BlockModelShapes.getModelLocation(state) }
        }.forEach { (state, stateModelResource) ->
            func(ModelDiscoveryContext(this, state, stateModelResource))
        }
    }
}

abstract class ConfigurableModelDiscovery<T> : ModelDiscovery<T>() {

    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    abstract fun processModel(state: BlockState, textures: List<String>, atlas: AtlasFuture): CompletableFuture<T>?

    override fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<T>? {
        val matchClass = matchClasses.matchingClass(ctx.state.block) ?: return null
        log("block state ${ctx.state.toString()}")
        log("      class ${ctx.state.block.javaClass.name} matches ${matchClass.name}")

        if (ctx.models.isEmpty()) {
            log("       no models found")
            return null
        }

        ctx.models.filter { it.first is BlockModel }.forEach { (model, location) ->
            model as BlockModel
            val modelMatch = modelTextures.firstOrNull { (model to location).derivesFrom(it.modelLocation) }
            if (modelMatch != null) {
                log("      model ${model} matches ${modelMatch.modelLocation}")

                val textures = modelMatch.textureNames.map { it to model.resolveTextureName(it) }
                val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
                log("    sprites [$texMapString]")

                if (textures.all { it.second != "missingno" }) {
                    // found a valid model (all required textures exist)
                    return processModel(ctx.state, textures.map { it.second}, atlas)
                }
            }
        }
        return null
    }

}
