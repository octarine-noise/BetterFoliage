package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.idx
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.lazy
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardLilypadDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        ctx.addReplacement(StandardLilypadKey)
    }
}

object StandardLilypadKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardLilypadModel(wrapped)
}

class LilypadRenderData(
    val rootIdx: Int,
    val flowerIdx: Int?
)

class StandardLilypadModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit
        return LilypadRenderData(
            rootIdx = random.idx(lilypadRootModels),
            flowerIdx = random.idxOrNull(lilypadFlowerModels) { Config.lilypad.enabled(random) }
        )
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        ctx.checkSides = false
        super.renderLayer(ctx, data, layer)
        if (data is LilypadRenderData) {
            data.flowerIdx?.let { ctx.renderQuads(lilypadFlowerModels[it]) }
            ShadersModIntegration.grass(ctx, Config.lilypad.shaderWind) {
                ctx.renderQuads(lilypadRootModels[data.rootIdx])
            }
        }
    }

    companion object {
        val lilypadRootSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_lilypad_roots_$idx")
        }
        val lilypadFlowerSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_lilypad_flower_$idx")
        }
        val lilypadRootModels by BetterFoliage.modelManager.lazy {
            val shapes = tuftShapeSet(1.0, 1.0, 1.0, Config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white, -1) { lilypadRootSprites[it] }
                .transform { move(2.0 to DOWN) }
                .buildTufts()
        }
        val lilypadFlowerModels by BetterFoliage.modelManager.lazy {
            val shapes = tuftShapeSet(0.5, 0.5, 0.5, Config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white, -1) { lilypadFlowerSprites[it] }
                .transform { move(1.0 to DOWN) }
                .buildTufts()
        }
    }
}