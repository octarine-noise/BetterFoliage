package mods.octarinecore.client.resource

import com.google.common.base.Joiner
import mods.betterfoliage.client.Client
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.filterValuesNotNull
import mods.octarinecore.findFirst
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper
import net.minecraft.client.renderer.block.statemap.IStateMapper
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

class LoadModelDataEvent(val loader: ModelLoader) : Event()

interface ModelRenderRegistry<T> {
    operator fun get(ctx: BlockContext) = get(ctx.blockState(Int3.zero), ctx.world!!, ctx.pos)
    operator fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos): T?
}

interface ModelRenderDataExtractor<T> {
    fun processModel(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): ModelRenderKey<T>?
}

interface ModelRenderKey<T> {
    val logger: Logger?
    fun onPreStitch(atlas: TextureMap) {}
    fun resolveSprites(atlas: TextureMap): T
}

abstract class ModelRenderRegistryRoot<T> : ModelRenderRegistry<T> {
    val subRegistries = mutableListOf<ModelRenderRegistry<T>>()
    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos) = subRegistries.findFirst { it[state, world, pos] }
    fun addRegistry(registry: ModelRenderRegistry<T>) {
        subRegistries.add(registry)
        MinecraftForge.EVENT_BUS.register(registry)
    }
}

abstract class ModelRenderRegistryBase<T> : ModelRenderRegistry<T>, ModelRenderDataExtractor<T> {
    open val logger: Logger? = null
    open val logName: String get() = this::class.java.name

    val stateToKey = mutableMapOf<IBlockState, ModelRenderKey<T>>()
    var stateToValue = mapOf<IBlockState, T>()

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos) = stateToValue[state]

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent
    fun handleLoadModelData(event: LoadModelDataEvent) {
        stateToValue = emptyMap()

        val stateMappings = Block.REGISTRY.flatMap { block ->
            val mapper = event.loader.blockModelShapes.blockStateMapper.blockStateMap[block] as? IStateMapper ?: DefaultStateMapper()
            (mapper.putStateModelLocations(block as Block) as Map<IBlockState, ModelResourceLocation>).entries
        }
        val stateModels = Refs.stateModels.get(event.loader) as Map<ModelResourceLocation, IModel>

        stateMappings.forEach { mapping ->
            if (mapping.key.block != null) stateModels[mapping.value]?.let { model ->
                try {
                    processModel(mapping.key, mapping.value, model)?.let { stateToKey[mapping.key] = it }
                } catch (e: Exception) {
                    logger?.warn("Exception while trying to process model ${mapping.value}", e)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        stateToKey.forEach { (_, key) -> key.onPreStitch(event.map) }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        stateToValue = stateToKey.mapValues { (_, key) -> key.resolveSprites(event.map) }
        stateToKey.clear()
    }
}

abstract class ModelRenderRegistryConfigurable<T> : ModelRenderRegistryBase<T>() {

    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    override fun processModel(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): ModelRenderKey<T>? {
        val matchClass = matchClasses.matchingClass(state.block) ?: return null
        logger?.log(Level.DEBUG, "$logName: block state ${state.toString()}")
        logger?.log(Level.DEBUG, "$logName:       class ${state.block.javaClass.name} matches ${matchClass.name}")

        val allModels = model.modelBlockAndLoc.distinctBy { it.second }
        if (allModels.isEmpty()) {
            logger?.log(Level.DEBUG, "$logName:       no models found")
            return null
        }

        allModels.forEach { blockLoc ->
            val modelMatch = modelTextures.firstOrNull { blockLoc.derivesFrom(it.modelLocation) }
            if (modelMatch != null) {
                logger?.log(Level.DEBUG, "$logName:       model ${blockLoc.second} matches ${modelMatch.modelLocation}")

                val textures = modelMatch.textureNames.map { it to blockLoc.first.resolveTextureName(it) }
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

    abstract fun processModel(state: IBlockState, textures: List<String>) : ModelRenderKey<T>?
}