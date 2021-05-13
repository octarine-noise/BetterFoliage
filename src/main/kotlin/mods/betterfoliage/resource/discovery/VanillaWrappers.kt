package mods.betterfoliage.resource.discovery

import mods.betterfoliage.model.HalfBakedSimpleModelWrapper
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.WeightedModelWrapper
import mods.betterfoliage.util.HasLogger
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.SimpleBakedModel
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN

class WeightedUnbakedKey(
    val replacements: Map<ResourceLocation, ModelBakingKey>
) : ModelBakingKey {

    override fun bake(ctx: ModelBakingContext): IBakedModel? {
        val unbaked = ctx.getUnbaked()
        if (unbaked !is VariantList) return super.bake(ctx)

        // bake all variants, replace as needed
        val bakedModels = unbaked.variantList.mapNotNull {
            val variantCtx = ctx.copy(location = it.modelLocation, transform = it)
            val replacement = replacements[it.modelLocation]
            val baked = replacement?.let { replacement ->
                ctx.logger.log(INFO, "Baking replacement for variant [${variantCtx.getUnbaked()::class.java.simpleName}] ${variantCtx.location} -> $replacement")
                replacement.bake(variantCtx)
            } ?: variantCtx.getBaked()
            when(baked) {
                is SpecialRenderModel -> it to baked
                // just in case we replaced some variants in the list, but not others
                // this should not realistically happen, this is just a best-effort fallback
                is SimpleBakedModel -> it to HalfBakedSimpleModelWrapper(baked)
                else -> null
            }
        }

        // something fishy going on, possibly unknown model type
        // let it through unchanged
        if (bakedModels.isEmpty()) return super.bake(ctx)

        if (bakedModels.size < unbaked.variantList.size) {
            detailLogger.log(
                WARN,
                "Dropped ${unbaked.variantList.size - bakedModels.size} variants from model ${ctx.location}"
            )
        }
        val weightedSpecials = bakedModels.map { (variant, model) ->
            WeightedModelWrapper.WeightedModel(model, variant.weight)
        }
        return WeightedModelWrapper(weightedSpecials, weightedSpecials[0].model)
    }

    override fun toString() = "[SpecialRenderVariantList, ${replacements.size} replacements]"

    companion object : HasLogger()
}