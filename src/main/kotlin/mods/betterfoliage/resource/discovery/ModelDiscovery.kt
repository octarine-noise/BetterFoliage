package mods.betterfoliage.resource.discovery

import com.google.common.base.Joiner
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.YarnHelper
import mods.betterfoliage.util.get
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.WeightedUnbakedModel
import net.minecraft.client.texture.MissingSprite
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.Level

abstract class AbstractModelDiscovery : HasLogger(), ModelDiscovery {
    override fun onModelsLoaded(
        bakery: ModelLoader,
        sprites: MutableSet<Identifier>,
        replacements: MutableMap<Identifier, ModelBakingKey>
    ) {
        Registry.BLOCK
            .flatMap { block -> block.stateManager.states }
            .forEach { state ->
                val location = BlockModels.getModelId(state)
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
        if (model is WeightedUnbakedModel) {
            // per-location replacements need to be scoped to the variant list, as replacement models
            // may need information from the BlockState which is not available at baking time
            val scopedReplacements = mutableMapOf<Identifier, ModelBakingKey>()
            model.variants.forEach { variant ->
                processModel(ctx.copy(modelLocation = variant.location, replacements = scopedReplacements))
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
        textureMatch: List<Identifier>
    )

    override fun processModel(ctx: ModelDiscoveryContext) {
        val model = ctx.getUnbaked()
        if (model is JsonUnbakedModel) {
            val matchClass = matchClasses.matchingClass(ctx.blockState.block) ?: return

            detailLogger.log(Level.INFO, "block state ${ctx.blockState}")
            detailLogger.log(Level.INFO, "      model ${ctx.modelLocation}")
            detailLogger.log(Level.INFO, "      class ${ctx.blockState.block.javaClass.name} matches ${matchClass.name}")

            modelTextures
                .filter { matcher -> ctx.bakery.modelDerivesFrom(model, ctx.modelLocation, matcher.modelLocation) }
                .forEach { match ->
                    detailLogger.log(Level.INFO, "      model $model matches ${match.modelLocation}")

                    val materials = match.textureNames.map { it to model.resolveSprite(it) }
                    val texMapString = Joiner.on(", ").join(materials.map { "${it.first}=${it.second.textureId}" })
                    detailLogger.log(Level.INFO, "    sprites [$texMapString]")

                    if (materials.all { it.second.textureId != MissingSprite.getMissingSpriteId() }) {
                        // found a valid model (all required textures exist)
                        processModel(ctx, materials.map { it.second.textureId })
                    }
                }
        }
        return super.processModel(ctx)
    }
}

// net.minecraft.client.render.model.json.JsonUnbakedModel.parentId
val JsonUnbakedModel_parentId = YarnHelper.requiredField<Identifier>("net.minecraft.class_793", "field_4247", "Lnet/minecraft/class_2960;")

fun ModelLoader.modelDerivesFrom(model: JsonUnbakedModel, location: Identifier, target: Identifier): Boolean =
    if (location == target) true
    else model[JsonUnbakedModel_parentId]
        ?.let { getOrLoadModel(it) as? JsonUnbakedModel }
        ?.let { parent -> modelDerivesFrom(parent, model[JsonUnbakedModel_parentId]!!, target) }
        ?: false
