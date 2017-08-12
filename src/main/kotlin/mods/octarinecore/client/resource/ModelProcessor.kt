package mods.octarinecore.client.resource

import com.google.common.base.Joiner
import mods.betterfoliage.loader.Refs
import mods.octarinecore.common.config.IBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.filterValuesNotNull
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelBlock
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper
import net.minecraft.client.renderer.block.statemap.IStateMapper
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger

class LoadModelDataEvent(val loader: ModelLoader) : Event()

data class ModelVariant(
    val state: IBlockState,
    var modelBlock: ModelBlock?,
    val modelLocation: ResourceLocation?,
    val weight: Int
)

interface ModelProcessor<T1, T2> {
    val logger: Logger?
    var variants: MutableMap<IBlockState, MutableList<ModelVariant>>
    var variantToKey: MutableMap<ModelVariant, T1>
    var variantToValue: Map<ModelVariant, T2>

    fun addVariant(state: IBlockState, variant: ModelVariant) { variants.getOrPut(state) { mutableListOf() }.add(variant) }
    fun getVariant(state: IBlockState, rand: Int) = variants[state]?.let { it[rand % it.size] }
    fun putKeySingle(state: IBlockState, key: T1) {
        val variant = ModelVariant(state, null, null, 1)
        variants[state] = mutableListOf(variant)
        variantToKey[variant] = key
    }

    fun onPostLoad() { }
    fun onPreStitch() { }

    fun processModelLoad1(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel)

    fun processStitch(variant: ModelVariant, key: T1, atlas: TextureMap): T2?

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun clearBeforeLoadModelData(event: LoadModelDataEvent) {
        variants.clear()
        variantToKey.clear()
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
                processModelLoad1(mapping.key, mapping.value, model)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        onPreStitch()
        variantToValue = variantToKey.mapValues { processStitch(it.key, it.value, event.map) }.filterValuesNotNull()
    }
}

interface TextureListModelProcessor<T2> : ModelProcessor<List<String>, T2> {
    val logName: String
    val matchClasses: IBlockMatcher
    val modelTextures: List<ModelTextureList>

    override fun processModelLoad1(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel) {
        val matchClass = matchClasses.matchingClass(state.block) ?: return
        logger?.log(Level.DEBUG, "$logName: block state ${state.toString()}")
        logger?.log(Level.DEBUG, "$logName:       class ${state.block.javaClass.name} matches ${matchClass.name}")

        val allModels = model.modelBlockAndLoc.distinctBy { it.second }
        if (allModels.isEmpty()) {
            logger?.log(Level.DEBUG, "$logName:       no models found")
            return
        }

        allModels.forEach { blockLoc ->
            val modelMatch = modelTextures.firstOrNull { blockLoc.derivesFrom(it.modelLocation) }
            if (modelMatch != null) {
                logger?.log(Level.DEBUG, "$logName:       model ${blockLoc.second} matches ${modelMatch.modelLocation}")

                val textures = modelMatch.textureNames.map { it to blockLoc.first.resolveTextureName(it) }
                val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
                logger?.log(Level.DEBUG, "$logName:       textures [$texMapString]")

                if (textures.all { it.second != "missingno" }) {
                    // found a valid variant (all required textures exist)
                    val variant = ModelVariant(state, blockLoc.first, blockLoc.second, 1)
                    addVariant(state, variant)
                    variantToKey[variant] = textures.map { it.second }
                }
            }
        }
    }

//    override fun processModelLoad(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel): List<String>? {
//        val matchClass = matchClasses.matchingClass(state.block) ?: return null
//        logger?.log(Level.DEBUG, "$logName: block state ${state.toString()}")
//        logger?.log(Level.DEBUG, "$logName:       class ${state.block.javaClass.name} matches ${matchClass.name}")
//
//        val allModels = model.modelBlockAndLoc
//        if (allModels.isEmpty()) {
//            logger?.log(Level.DEBUG, "$logName:       no models found")
//            return null
//        }
//        allModels.forEach { blockLoc ->
//            modelTextures.firstOrNull { blockLoc.derivesFrom(it.modelLocation) }?.let{ modelMatch ->
//                logger?.log(Level.DEBUG, "$logName:       model ${blockLoc.second} matches ${modelMatch.modelLocation.toString()}")
//
//                val textures = modelMatch.textureNames.map { it to blockLoc.first.resolveTextureName(it) }
//                val texMapString = Joiner.on(", ").join(textures.map { "${it.first}=${it.second}" })
//                logger?.log(Level.DEBUG, "$logName:       textures [$texMapString]")
//
//                return if (textures.all { it.second != "missingno" }) textures.map { it.second } else null
//            }
//        }
//        logger?.log(Level.DEBUG, "$logName:       no matching models found")
//        return null
//    }
}

interface TextureMediatedRegistry<T1, T3> : ModelProcessor<T1, TextureAtlasSprite> {

    var textureToValue: MutableMap<TextureAtlasSprite, T3>

    @Suppress("UNCHECKED_CAST")
    override fun handlePreStitch(event: TextureStitchEvent.Pre) {
        textureToValue.clear()
        super.handlePreStitch(event)

        val textureToVariants = variantToValue.entries.groupBy(keySelector = { it.value }, valueTransform = { it.key })
        variantToValue.values.toSet().forEach { processTexture(textureToVariants[it]!!, it, event.map) }
    }

    fun processTexture(states: List<ModelVariant>, texture: TextureAtlasSprite, atlas: TextureMap)
}