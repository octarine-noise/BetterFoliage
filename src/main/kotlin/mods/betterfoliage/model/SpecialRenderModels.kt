package mods.betterfoliage.model

import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.util.HasLogger
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraft.util.WeightedRandom
import org.apache.logging.log4j.Level.WARN
import java.util.Random
import java.util.function.Function

/**
 * Model that makes use of advanced rendering features.
 */
interface SpecialRenderModel : IBakedModel {
    fun render(ctx: RenderCtxBase, noDecorations: Boolean = false)
}

class SpecialRenderVariantList(
    val models: List<WeightedModel>, baseModel: SpecialRenderModel
): IBakedModel by baseModel, SpecialRenderModel {
    class WeightedModel(val model: SpecialRenderModel, weight: Int) : WeightedRandom.Item(weight)
    val totalWeight = models.sumBy { it.itemWeight }

    fun getModel(random: Random) = WeightedRandom.getRandomItem(models, random.nextInt(totalWeight))

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        getModel(ctx.random).model.render(ctx, noDecorations)
    }

    companion object : HasLogger() {
        /**
         * If any of the variants in this [VariantList] bake to [SpecialRenderModel], give back a
         * [SpecialRenderVariantList] so that variants can take advantage of extra features.
         * Otherwise, give back null.
         */
        fun bakeIfSpecial(
            location: ResourceLocation,
            variantModel: VariantList,
            bakery: ModelBakery,
            spriteGetter: Function<Material, TextureAtlasSprite>
        ): SpecialRenderVariantList? {
            val bakedModels = variantModel.variantList.map {
                it to bakery.getBakedModel(it.modelLocation, it, spriteGetter)
            }.filter { it.second != null }

            if (bakedModels.all { it.second !is SpecialRenderModel }) return null
            val weightedSpecials = bakedModels.mapNotNull { (variant, model) ->
                when (model) {
                    is SpecialRenderModel -> WeightedModel(model, variant.weight)
                    else -> null
                }
            }
            if (bakedModels.size > weightedSpecials.size) {
                detailLogger.log(WARN, "Dropped ${bakedModels.size - weightedSpecials.size} variants from model $location")
            }
            return SpecialRenderVariantList(weightedSpecials, weightedSpecials[0].model)
        }
    }
}