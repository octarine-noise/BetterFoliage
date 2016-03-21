package mods.octarinecore.client.resource

import mods.betterfoliage.client.config.BlockMatcher
import mods.betterfoliage.loader.Refs
import mods.octarinecore.stripStart
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
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class LoadModelDataEvent(val loader: ModelLoader) : Event()

abstract class ModelDataInspector {

    abstract fun onAfterModelLoad()
    abstract fun processModelDefinition(state: IBlockState, location: ModelResourceLocation, model: IModel)
    abstract fun onStitch(atlas: TextureMap)

    init { MinecraftForge.EVENT_BUS.register(this) }

    @Suppress("UNCHECKED_CAST")
    @SubscribeEvent
    fun handleLoadModelData(event: LoadModelDataEvent) {
        val stateMappings = Block.blockRegistry.flatMap { block ->
            ((event.loader.blockModelShapes.blockStateMapper.blockStateMap[block] as? IStateMapper ?: DefaultStateMapper())
                .putStateModelLocations(block as Block) as Map<IBlockState, ModelResourceLocation>).entries
        }
        val stateModels = Refs.stateModels.get(event.loader) as Map<ModelResourceLocation, IModel>

        onAfterModelLoad()

        stateMappings.forEach { mapping ->
            stateModels[mapping.value]?.let { processModelDefinition(mapping.key, mapping.value, it) }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handleTextureReload(event: TextureStitchEvent.Pre) { onStitch(event.map) }
}

abstract class BlockTextureInspector<T> : ModelDataInspector() {

    val state2Names = hashMapOf<IBlockState, Iterable<String>>()
    val modelMappings = mutableListOf<Pair<(IBlockState, IModel)->Boolean, Iterable<String>>>()
    val stateMap = hashMapOf<IBlockState, T>()

    fun match(textureNames: Iterable<String>, predicate: (IBlockState, IModel)->Boolean) =
        modelMappings.add(predicate to textureNames)
    fun matchClassAndModel(blockClass: BlockMatcher, modelLocation: String, textureNames: Iterable<String>) =
        match(textureNames) { state, model -> blockClass.matchesClass(state.block) && model.derivesFromModel(modelLocation) }

    operator fun get(state: IBlockState) = stateMap[state]

    override fun onAfterModelLoad() {
        stateMap.clear()
    }

    override fun processModelDefinition(state: IBlockState, modelDefLoc: ModelResourceLocation, model: IModel) {
        modelMappings.forEach { mapping ->
            if (mapping.first(state, model)) {
                model.modelBlockAndLoc?.first?.let { modelBlock ->
                    val textures = mapping.second.map { modelBlock.resolveTextureName(it) }
                    if (textures.all { it != null && it != "missingno" }) {
                        state2Names.put(state, textures)
                    }
                }
            }
        }
    }

    override fun onStitch(atlas: TextureMap) {
        val state2Texture = hashMapOf<IBlockState, List<TextureAtlasSprite>>()
        val texture2Info = hashMapOf<List<TextureAtlasSprite>, T>()
        state2Names.forEach { state, textureNames ->
            val textures = textureNames.map { atlas.getTextureExtry(ResourceLocation(it).toString()) }
            if (textures.all { it != null }) {
                state2Texture.put(state, textures)
                if (textures !in texture2Info) texture2Info.put(textures, processTextures(state, textures, atlas))
            }
        }
        state2Texture.forEach { state, texture -> stateMap.put(state, texture2Info[texture]!!) }
        state2Names.clear()
    }

    abstract fun processTextures(state: IBlockState, textures: List<TextureAtlasSprite>, atlas: TextureMap): T
}

@Suppress("UNCHECKED_CAST")
val IModel.modelBlockAndLoc: Pair<ModelBlock, ResourceLocation>? get() {
    if (Refs.VanillaModelWrapper.isInstance(this))
        return Pair(Refs.model_VMW.get(this) as ModelBlock, Refs.location_VMW.get(this) as ResourceLocation)
    else if (Refs.WeightedPartWrapper.isInstance(this)) Refs.model_WPW.get(this)?.let {
        return (it as IModel).modelBlockAndLoc
    }
    else if (Refs.WeightedRandomModel.isInstance(this)) Refs.models_WRM.get(this)?.let {
        (it as List<IModel>).forEach {
            it.modelBlockAndLoc.let { if (it != null) return it }
        }
    }
    return null
}

fun Pair<ModelBlock, ResourceLocation>.derivesFrom(targetLocation: String): Boolean {
    if (second.stripStart("models/") == ResourceLocation(targetLocation)) return true
    if (first.parent != null && first.parentLocation != null)
        return Pair(first.parent, first.parentLocation).derivesFrom(targetLocation)
    return false
}

fun IModel.derivesFromModel(modelLocation: String) = modelBlockAndLoc?.derivesFrom(modelLocation) ?: false