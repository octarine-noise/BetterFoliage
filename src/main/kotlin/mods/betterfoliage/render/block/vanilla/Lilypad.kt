package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.LILYPAD_BLOCKS
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.idx
import mods.betterfoliage.util.idxOrNull
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardLilypadDiscovery : AbstractModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is BlockModel && ctx.blockState.block in LILYPAD_BLOCKS) {
            ctx.addReplacement(StandardLilypadKey)
        }
        super.processModel(ctx)
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
        val lilypadRootModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = tuftShapeSet(1.0, 1.0, 1.0, Config.lilypad.hOffset)
            tuftModelSet(shapes, -1) { lilypadRootSprites[it] }
                .transform { move(2.0 to DOWN) }
                .buildTufts()
        }
        val lilypadFlowerModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = tuftShapeSet(0.5, 0.5, 0.5, Config.lilypad.hOffset)
            tuftModelSet(shapes, -1) { lilypadFlowerSprites[it] }
                .transform { move(1.0 to DOWN) }
                .buildTufts()
        }
    }
}