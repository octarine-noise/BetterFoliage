package mods.betterfoliage.render

import mods.betterfoliage.render.pipeline.RenderCtxBase
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.Material
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.model.VariantList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.WeightedRandom
import java.util.Random
import java.util.function.Function

interface ISpecialRenderModel : IBakedModel {
    fun render(ctx: RenderCtxBase, noDecorations: Boolean = false)
}

open class SpecialRenderWrapper(val baseModel: IBakedModel) : IBakedModel by baseModel, ISpecialRenderModel {
    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        ctx.renderFallback(baseModel)
    }
}

/**
 * If any of the variants in this [VariantList] bake to [ISpecialRenderModel], give back a
 * [SpecialRenderVariantList] so that variants can take advantage of extra features.
 * Otherwise, give back null.
 */
fun VariantList.bakeSpecial(bakery: ModelBakery, spriteGetter: Function<Material, TextureAtlasSprite>): SpecialRenderVariantList? {
    val bakedModels = variantList.map { bakery.getBakedModel(it.modelLocation, it, spriteGetter) }
    if (bakedModels.all { it !is ISpecialRenderModel }) return null
    val weightedItems = (variantList zip bakedModels)
        .filter { it.second != null }
        .map { (variant, model) ->
            val modelWrapped = (model!! as? ISpecialRenderModel) ?: SpecialRenderWrapper(model)
            SpecialRenderVariantList.WeightedModel(modelWrapped, variant.weight)
        }
    return SpecialRenderVariantList(weightedItems, weightedItems[0].model)
}

open class SpecialRenderVariantList(
    val models: List<WeightedModel>, baseModel: ISpecialRenderModel
): IBakedModel by baseModel, ISpecialRenderModel {
    class WeightedModel(val model: ISpecialRenderModel, weight: Int) : WeightedRandom.Item(weight)
    val totalWeight = models.sumBy { it.itemWeight }

    fun getModel(random: Random) = WeightedRandom.getRandomItem(models, random.nextInt(totalWeight))
    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) = getModel(ctx.random).model.render(ctx, noDecorations)
}