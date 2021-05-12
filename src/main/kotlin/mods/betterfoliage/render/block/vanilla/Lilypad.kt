package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
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
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation

object StandardLilypadDiscovery : AbstractModelDiscovery() {
    val LILYPAD_BLOCKS = listOf(Blocks.LILY_PAD)

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

class StandardLilypadModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        ctx.checkSides = false
        super.render(ctx, noDecorations)
        if (!Config.enabled || !Config.lilypad.enabled) return

        ShadersModIntegration.grass(ctx, Config.lilypad.shaderWind) {
            ctx.renderQuads(lilypadRootModels[ctx.random])
        }
        if (Config.lilypad.enabled(ctx.random)) ctx.renderQuads(lilypadFlowerModels[ctx.random])
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