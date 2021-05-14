package mods.betterfoliage.model

import mods.betterfoliage.render.pipeline.RenderCtxBase
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.util.WeightedRandom
import java.util.Random

/**
 * Model that makes use of advanced rendering features.
 */
interface SpecialRenderModel : IBakedModel {
    fun render(ctx: RenderCtxBase, noDecorations: Boolean = false)
}

class WeightedModelWrapper(
    val models: List<WeightedModel>, baseModel: SpecialRenderModel
): IBakedModel by baseModel, SpecialRenderModel {
    class WeightedModel(val model: SpecialRenderModel, weight: Int) : WeightedRandom.Item(weight)
    val totalWeight = models.sumBy { it.weight }

    fun getModel(random: Random) = WeightedRandom.getWeightedItem(models, random.nextInt(totalWeight))

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        getModel(ctx.random).model.render(ctx, noDecorations)
    }
}
