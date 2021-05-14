package mods.betterfoliage.resource.discovery

import com.google.common.base.Joiner
import mods.betterfoliage.util.HasLogger
import net.minecraft.client.renderer.BlockModelShapes
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.client.renderer.texture.MissingTextureSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.Level

abstract class AbstractModelDiscovery : HasLogger(), ModelDiscovery {
    override fun onModelsLoaded(
        bakery: ModelBakery,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ) {
        ForgeRegistries.BLOCKS
            .flatMap { block -> block.stateDefinition.possibleStates }
            .forEach { state ->
                val location = BlockModelShapes.stateToModelLocation(state)
                val ctx = ModelDiscoveryContext(bakery, state, location, sprites, replacements, detailLogger)
                try {
                    processModel(ctx)
                } catch (e: Exception) {
                    logger.log(Level.WARN, "Discovery error in $location", e)
                }
            }
    }

    open fun processModel(ctx: ModelDiscoveryContext) {
        val model = ctx.getUnbaked()

        // built-in support for container models
        if (model is VariantList) {
            // per-location replacements need to be scoped to the variant list, as replacement models
            // may need information from the BlockState which is not available at baking time
            val scopedReplacements = mutableMapOf<ResourceLocation, ModelBakingKey>()
            model.variants.forEach { variant ->
                processModel(ctx.copy(modelLocation = variant.modelLocation, replacements = scopedReplacements))
            }
            if (scopedReplacements.isNotEmpty()) {
                ctx.addReplacement(WeightedUnbakedKey(scopedReplacements), addToStateKeys = false)
            }
        }
    }
}

abstract class ConfigurableModelDiscovery : AbstractModelDiscovery() {
    abstract val matchClasses: IBlockMatcher
    abstract val modelTextures: List<ModelTextureList>

    abstract fun processModel(
        ctx: ModelDiscoveryContext,
        textureMatch: List<ResourceLocation>
    )

    override fun processModel(ctx: ModelDiscoveryContext) {
        val model = ctx.getUnbaked()
        if (model is BlockModel) {
            val matchClass = matchClasses.matchingClass(ctx.blockState.block) ?: return

            detailLogger.log(Level.INFO, "block state ${ctx.blockState}")
            detailLogger.log(Level.INFO, "      model ${ctx.modelLocation}")
            detailLogger.log(Level.INFO, "      class ${ctx.blockState.block.javaClass.name} matches ${matchClass.name}")

            val ancestry = ctx.bakery.getAncestry(ctx.modelLocation)
            val matches = modelTextures.filter { matcher ->
                matcher.modelLocation in ancestry
            }
            matches.forEach { match ->
                    detailLogger.log(Level.INFO, "      model $model matches ${match.modelLocation}")

                val materials = match.textureNames.map { it to model.getMaterial(it) }
                val texMapString = Joiner.on(", ").join(materials.map { "${it.first}=${it.second.texture()}" })
                detailLogger.log(Level.INFO, "    sprites [$texMapString]")

                if (materials.all { it.second.texture() != MissingTextureSprite.getLocation() }) {
                    // found a valid model (all required textures exist)
                    processModel(ctx, materials.map { it.second.texture() })
                }
            }
            if (matches.isEmpty()) {
                detailLogger.log(Level.INFO, "      no matches for model ${ctx.modelLocation}, inheritance chain ${ancestry.joinToString(" -> ")}")
            }
        }
        return super.processModel(ctx)
    }
}

fun ModelBakery.modelDerivesFrom(model: BlockModel, location: ResourceLocation, target: ResourceLocation): Boolean =
    if (location == target) true
    else model.parentLocation
        ?.let { getModel(it) as? BlockModel }
        ?.let { parent -> modelDerivesFrom(parent, model.parentLocation!!, target) }
        ?: false

fun ModelBakery.getAncestry(location: ResourceLocation): List<ResourceLocation> {
    val model = getModel(location) as? BlockModel ?: return listOf(location)
    val parentAncestry = model.parentLocation?.let { getAncestry(it) } ?: emptyList()
    return listOf(location) + parentAncestry
}