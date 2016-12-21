package mods.octarinecore.client.resource

import com.google.common.base.Joiner
import mods.betterfoliage.loader.Refs
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.filterValuesNotNull
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper
import net.minecraft.client.renderer.block.statemap.IStateMapper
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

class LoadModelDataEvent(val loader: ModelLoader) : Event()

interface ModelProcessor<T1, T2> {
    val logger: Logger?
    var stateToKey: MutableMap<IBlockState, T1>
    var stateToValue: Map<IBlockState, T2>

    fun onPostLoad() { }
    fun onPreStitch() { }

    fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): T1?
    fun processStitch(state: IBlockState, key: T1, atlas: TextureMap): T2?

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun clearBeforeLoadModelData(event: LoadModelDataEvent) {
        stateToKey.clear()
    }

    @SubscribeEvent
    fun handleLoadModelData(event: LoadModelDataEvent) {
        onPostLoad()

        val stateMappings = Block.REGISTRY.flatMap { block ->
            val mapper = event.loader.blockModelShapes.blockStateMapper.blockStateMap[block] as? IStateMapper ?: DefaultStateMapper()
            (mapper.putStateModelLocations(block as Block) as Map<IBlockState, ModelResourceLocation>).entries
        }
        val stateModels = Refs.stateModels.get(event.loader) as Map<ModelResourceLocation, IModel>

        stateMappings.forEach { mapping ->
            if (mapping.key.block != null) stateModels[mapping.value]?.let { model ->
                processModelLoad(mapping.key, mapping.value, model)?.let { key -> stateToKey.put(mapping.key, key) }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        onPreStitch()
        stateToValue = stateToKey.mapValues { processStitch(it.key, it.value, event.map) }.filterValuesNotNull()
    }
}

interface TextureListModelProcessor<T2> : ModelProcessor<List<String>, T2> {
    val logName: String
    val matchClasses: IBlockMatcher
    val modelTextures: List<ModelTextureList>

    override fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): List<String>? {
        val matchClass = matchClasses.matchingClass(state.block) ?: return null
        logger?.log(Level.DEBUG, "$logName: block state ${state.toString()}")
        logger?.log(Level.DEBUG, "$logName:       class ${state.block.javaClass.name} matches ${matchClass.name}")

        val blockLoc = model.modelBlockAndLoc
        if (blockLoc == null) {
            logger?.log(Level.DEBUG, "$logName:       no models found")
            return null
        }
        val modelMatch = modelTextures.firstOrNull { blockLoc.derivesFrom(it.modelLocation) }
        if (modelMatch == null) {
            logger?.log(Level.DEBUG, "$logName:       no matching models found")
            return null
        }
        logger?.log(Level.DEBUG, "$logName:       model ${blockLoc.second} matches ${modelMatch.modelLocation.toString()}")

        val textures = modelMatch.textureNames.map { it to blockLoc.first.resolveTextureName(it) }
        val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
        logger?.log(Level.DEBUG, "$logName:       textures [$texMapString]")

        return if (textures.all { it.second != "missingno" }) textures.map { it.second } else null
    }
}

interface TextureMediatedRegistry<T1, T3> : ModelProcessor<T1, TextureAtlasSprite> {

    var textureToValue: MutableMap<TextureAtlasSprite, T3>

    @Suppress("UNCHECKED_CAST")
    override fun handlePreStitch(event: TextureStitchEvent.Pre) {
        textureToValue.clear()
        super.handlePreStitch(event)

        val textureToStates = stateToValue.entries.groupBy(keySelector = { it.value }, valueTransform = { it.key })
        stateToValue.values.toSet().forEach { processTexture(textureToStates[it]!!, it, event.map) }
    }

    fun processTexture(states: List<IBlockState>, texture: TextureAtlasSprite, atlas: TextureMap)
}