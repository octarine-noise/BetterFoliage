package mods.betterfoliage.resource.discovery

import com.google.common.base.Joiner
import mods.betterfoliage.config.IBlockMatcher
import mods.betterfoliage.config.ModelTextureList
import mods.betterfoliage.util.HasLogger
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelShapes
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.client.renderer.model.multipart.Multipart
import net.minecraft.client.renderer.texture.MissingTextureSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level

abstract class ModelReplacer : HasLogger(), ModelDiscovery {
    override fun onModelsLoaded(
        bakery: ModelBakery,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ) {
        ForgeRegistries.BLOCKS
            .flatMap { block -> block.stateContainer.validStates }
            .forEach { state ->
                val location = BlockModelShapes.getModelLocation(state)
                try {
                    val hasReplaced = processModel(bakery, state, location, sprites, replacements)
                } catch (e: Exception) {
                    logger.log(Level.WARN, "Discovery error in $location", e)
                }
            }
    }

    open fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ): Boolean {
        // built-in support for container models
        return when (val model = bakery.getUnbakedModel(location)) {
            is VariantList -> {
                val hasReplaced = model.variantList.fold(false) { hasReplaced, variant ->
                    processModel(bakery, state, variant.modelLocation, sprites, replacements) || hasReplaced
                }
                if (hasReplaced) replacements[location]
                hasReplaced
            }
            is Multipart -> model.variants.fold(false) { hasReplaced, variantList ->
                variantList.variantList.fold(false) { hasReplaced, variant ->
                    processModel(bakery, state, variant.modelLocation, sprites, replacements) || hasReplaced
                } || hasReplaced
            }
            else -> false
        }
    }
}



abstract class ConfigurableModelReplacer : ModelReplacer() {
    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    abstract fun processModel(
        state: BlockState,
        location: ResourceLocation,
        textureMatch: List<ResourceLocation>,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ): Boolean

    override fun processModel(
        bakery: ModelBakery,
        state: BlockState,
        location: ResourceLocation,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ): Boolean {
        val model = bakery.getUnbakedModel(location)
        if (model is BlockModel) {
            val matchClass = matchClasses.matchingClass(state.block) ?: return false

            detailLogger.log(Level.INFO, "block state $state")
            detailLogger.log(Level.INFO, "      model $location")
            replacements[location]?.let { existing ->
                detailLogger.log(Level.INFO, "      already processed as $existing")
                return true
            }

            detailLogger.log(Level.INFO, "      class ${state.block.javaClass.name} matches ${matchClass.name}")

            modelTextures
                .filter { matcher -> bakery.modelDerivesFrom(model, location, matcher.modelLocation) }
                .forEach { match ->
                    detailLogger.log(Level.INFO, "      model ${model} matches ${match.modelLocation}")

                    val materials = match.textureNames.map { it to model.resolveTextureName(it) }
                    val texMapString = Joiner.on(", ").join(materials.map { "${it.first}=${it.second.textureLocation}" })
                    detailLogger.log(Level.INFO, "    sprites [$texMapString]")

                    if (materials.all { it.second.textureLocation != MissingTextureSprite.getLocation() }) {
                        // found a valid model (all required textures exist)
                        if (processModel(state, location, materials.map { it.second.textureLocation }, sprites, replacements))
                            return true
                    }
                }
        }
        return super.processModel(bakery, state, location, sprites, replacements)
    }
}

fun ModelBakery.modelDerivesFrom(model: BlockModel, location: ResourceLocation, target: ResourceLocation): Boolean =
    if (location == target) true
    else model.parentLocation
        ?.let { getUnbakedModel(it) as? BlockModel }
        ?.let { parent -> modelDerivesFrom(parent, model.parentLocation!!, target) }
        ?: false
