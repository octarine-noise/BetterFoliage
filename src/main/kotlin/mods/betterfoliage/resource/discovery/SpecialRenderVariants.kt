package mods.betterfoliage.resource.discovery

import mods.betterfoliage.model.HalfBakedSimpleModelWrapper
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpecialRenderVariantList
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.SimpleBakedModel
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN

class SpecialRenderVariantList(
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
                is SimpleBakedModel -> it to HalfBakedSimpleModelWrapper(baked)
                else -> null
            }
        }

        // something fishy going on, possibly unknown model type
        // let it through unchanged
        if (bakedModels.isEmpty()) return super.bake(ctx)

        if (bakedModels.size < unbaked.variantList.size) {
            SpecialRenderVariantList.detailLogger.log(
                WARN,
                "Dropped ${unbaked.variantList.size - bakedModels.size} variants from model ${ctx.location}"
            )
        }
        val weightedSpecials = bakedModels.map { (variant, model) ->
            SpecialRenderVariantList.WeightedModel(model, variant.weight)
        }
        return SpecialRenderVariantList(weightedSpecials, weightedSpecials[0].model)
    }

    override fun toString() = "[SpecialRenderVariantList, ${replacements.size} replacements]"
}