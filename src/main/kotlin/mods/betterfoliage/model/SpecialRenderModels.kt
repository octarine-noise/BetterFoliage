package mods.betterfoliage.model

import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.render.pipeline.RenderCtxBase
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.WeightedBakedModel
import net.minecraft.util.WeightedRandom
import java.util.Random

/**
 * Model that makes use of advanced rendering features.
 */
interface SpecialRenderModel : IBakedModel {
    /**
     * Create custom renderdata object. Called once per block. Result is passed to renderLayer().
     */
    fun prepare(ctx: BlockCtx, random: Random): Any
    fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType)

    /**
     * Get the actual model that will be rendered. Useful for container models (like [WeightedBakedModel]).
     */
    fun resolve(random: Random) = this
}

interface SpecialRenderData {
    fun canRenderInLayer(layer: RenderType) = false
}

class WeightedModelWrapper(
    val models: List<WeightedModel>, baseModel: SpecialRenderModel
) : IBakedModel by baseModel, SpecialRenderModel {
    class WeightedModel(val model: SpecialRenderModel, weight: Int) : WeightedRandom.Item(weight)

    val totalWeight = models.sumBy { it.weight }

    fun getModel(random: Random) = WeightedRandom.getWeightedItem(models, random.nextInt(totalWeight))

    override fun resolve(random: Random) = getModel(random).model.resolve(random)

    override fun prepare(ctx: BlockCtx, random: Random) = getModel(random).model.let { actual ->
        WeightedRenderData(actual, actual.prepare(ctx, random))
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) = when (data) {
        is WeightedRenderData -> data.model.renderLayer(ctx, data.modelRenderData, layer)
        else -> getModel(ctx.random).model.renderLayer(ctx, data, layer)
    }
}

data class WeightedRenderData(
    val model: SpecialRenderModel,
    val modelRenderData: Any
) : SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = (modelRenderData as? SpecialRenderData)?.canRenderInLayer(layer) ?: false
}