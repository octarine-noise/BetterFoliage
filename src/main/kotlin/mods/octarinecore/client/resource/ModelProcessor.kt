package mods.octarinecore.client.resource

import com.google.common.base.Joiner
import mods.octarinecore.client.render.BlockCtx
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.common.plus
import mods.octarinecore.findFirst
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelShapes
import net.minecraft.client.renderer.model.*
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEnviromentBlockReader
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

class LoadModelDataEvent(val bakery: ModelBakery) : Event()

interface ModelRenderRegistry<T> {
    operator fun get(ctx: BlockCtx) = get(ctx.state(Int3.zero), ctx.world, ctx.pos)
    operator fun get(ctx: BlockCtx, offset: Int3) = get(ctx.state(offset), ctx.world, ctx.pos + offset)
    operator fun get(state: BlockState, world: IBlockReader, pos: BlockPos): T?
}

interface ModelRenderDataExtractor<T> {
    fun processModel(state: BlockState, modelLoc: ModelResourceLocation, models: List<Pair<IUnbakedModel, ResourceLocation>>): ModelRenderKey<T>?
}

interface ModelRenderKey<T> {
    val logger: Logger?
    fun onPreStitch(event: TextureStitchEvent.Pre) {}
    fun resolveSprites(atlas: AtlasTexture): T
}

abstract class ModelRenderRegistryRoot<T> : ModelRenderRegistry<T> {
    val subRegistries = mutableListOf<ModelRenderRegistry<T>>()
    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos) = subRegistries.findFirst { it[state, world, pos] }
    fun addRegistry(registry: ModelRenderRegistry<T>) {
        subRegistries.add(registry)
    }
}

abstract class ModelRenderRegistryBase<T> : ModelRenderRegistry<T>, ModelRenderDataExtractor<T> {
    open val logger: Logger? = null
    open val logName: String get() = this::class.java.name

    val stateToKey = mutableMapOf<BlockState, ModelRenderKey<T>>()
    var stateToValue = mapOf<BlockState, T>()

    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos) = stateToValue[state]

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent
    fun handleLoadModelData(event: LoadModelDataEvent) {
        stateToValue = emptyMap()

        val stateMappings = ForgeRegistries.BLOCKS.flatMap { block ->
            block.stateContainer.validStates.map { state -> state to BlockModelShapes.getModelLocation(state) }
        }
//        val unbakedModels = (Refs.unbakedModels.get(event.loader) as Map<*, *>).filter { it.value is IUnbakedModel } as Map<ModelResourceLocation, IUnbakedModel>

        val missingModel = event.bakery.getUnbakedModel(ModelBakery.MODEL_MISSING)
        stateMappings.forEach { (state, stateModelResource) ->
            val allModels = event.bakery.let { it.unwrapVariants(it.getUnbakedModel(stateModelResource) to stateModelResource) }.filter { it.second != missingModel }
            try {
                processModel(state, stateModelResource, allModels)?.let { stateToKey[state] = it }
            } catch (e: Exception) {
                logger?.warn("Exception while trying to process model ${stateModelResource}", e)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        if (event.map.basePath != "textures") return
        stateToKey.forEach { (_, key) -> key.onPreStitch(event) }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        if (event.map.basePath != "textures") return
        stateToValue = stateToKey.mapValues { (_, key) -> key.resolveSprites(event.map) }
        stateToKey.clear()
    }
}

abstract class ModelRenderRegistryConfigurable<T> : ModelRenderRegistryBase<T>() {

    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    override fun processModel(state: BlockState, modelLoc: ModelResourceLocation, models: List<Pair<IUnbakedModel, ResourceLocation>>): ModelRenderKey<T>? {
        val matchClass = matchClasses.matchingClass(state.block) ?: return null
        logger?.log(Level.DEBUG, "$logName: block state ${state.toString()}")
        logger?.log(Level.DEBUG, "$logName:       class ${state.block.javaClass.name} matches ${matchClass.name}")

        if (models.isEmpty()) {
            logger?.log(Level.DEBUG, "$logName:       no models found")
            return null
        }

        models.filter { it.first is BlockModel }.forEach { (model, location) ->
            model as BlockModel
            val modelMatch = modelTextures.firstOrNull { (model to location).derivesFrom(it.modelLocation) }
            if (modelMatch != null) {
                logger?.log(Level.DEBUG, "$logName:       model ${model} matches ${modelMatch.modelLocation}")

                val textures = modelMatch.textureNames.map { it to model.resolveTextureName(it) }
                val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
                logger?.log(Level.DEBUG, "$logName:       textures [$texMapString]")

                if (textures.all { it.second != "missingno" }) {
                    // found a valid model (all required textures exist)
                    return processModel(state, textures.map { it.second} )
                }
            }
        }
        return null
    }

    abstract fun processModel(state: BlockState, textures: List<String>) : ModelRenderKey<T>?
}

fun ModelBakery.unwrapVariants(modelAndLoc: Pair<IUnbakedModel, ResourceLocation>): List<Pair<IUnbakedModel, ResourceLocation>> = when(val model = modelAndLoc.first) {
    is VariantList -> model.variantList.flatMap { variant -> unwrapVariants(getUnbakedModel(variant.modelLocation) to variant.modelLocation) }
    is BlockModel -> listOf(modelAndLoc)
    else -> emptyList()
}
