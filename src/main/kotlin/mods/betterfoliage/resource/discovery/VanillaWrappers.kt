package mods.betterfoliage.resource.discovery

import mods.betterfoliage.model.WeightedModelWrapper
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.WrappedMeshModel
import mods.betterfoliage.util.HasLogger
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.WeightedUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.collection.Weighted
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN

class WeightedUnbakedKey(
    val replacements: Map<Identifier, ModelBakingKey>
) : ModelBakingKey {

    override fun bake(ctx: ModelBakingContext): BakedModel? {
        val unbaked = ctx.getUnbaked()
        if (unbaked !is WeightedUnbakedModel) return super.bake(ctx)

        // bake all variants, replace as needed
        val bakedModels = unbaked.variants.mapNotNull {
            val variantCtx = ctx.copy(location = it.location, transform = it)
            val replacement = replacements[it.location]
            val baked = replacement?.let { replacement ->
                ctx.logger.log(INFO, "Baking replacement for variant [${variantCtx.getUnbaked()::class.java.simpleName}] ${variantCtx.location} -> $replacement")
                replacement.bake(variantCtx)
            } ?: variantCtx.getBaked()
            when(baked) {
                is WrappedBakedModel -> it to baked
                // just in case we replaced some variants in the list, but not others
                // this should not realistically happen, this is just a best-effort fallback
                is BasicBakedModel -> it to WrappedMeshModel.converter(
                    state = null, unshade = false, noDiffuse = true, blendModeOverride = BlendMode.CUTOUT_MIPPED
                ).convert(baked)!!
                else -> null
            }
        }

        // something fishy going on, possibly unknown model type
        // let it through unchanged
        if (bakedModels.isEmpty()) return super.bake(ctx)

        if (bakedModels.size < unbaked.variants.size) {
            detailLogger.log(
                WARN,
                "Dropped ${unbaked.variants.size - bakedModels.size} variants from model ${ctx.location}"
            )
        }
        val weightedSpecials = bakedModels.map { (variant, model) ->
            Weighted.of(model, variant.weight)
        }
        return WeightedModelWrapper(weightedSpecials, weightedSpecials[0].data)
    }

    override fun toString() = "[WeightedUnbakedKey, ${replacements.size} replacements]"

    companion object : HasLogger()
}