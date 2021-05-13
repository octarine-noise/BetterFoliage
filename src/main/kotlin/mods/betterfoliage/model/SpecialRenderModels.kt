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

class WeightedModelWrapper(
    val models: List<WeightedModel>, baseModel: SpecialRenderModel
): IBakedModel by baseModel, SpecialRenderModel {
    class WeightedModel(val model: SpecialRenderModel, weight: Int) : WeightedRandom.Item(weight)
    val totalWeight = models.sumBy { it.itemWeight }

    fun getModel(random: Random) = WeightedRandom.getRandomItem(models, random.nextInt(totalWeight))

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        getModel(ctx.random).model.render(ctx, noDecorations)
    }
}
