package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.meshifyCutoutMipped
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Supplier

object StandardLilypadDiscovery : AbstractModelDiscovery() {
    val LILYPAD_BLOCKS = listOf(Blocks.LILY_PAD)

    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is JsonUnbakedModel && ctx.blockState.block in LILYPAD_BLOCKS) {
            ctx.addReplacement(StandardLilypadKey)
        }
        super.processModel(ctx)
    }
}

object StandardLilypadKey : ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = StandardLilypadModel(meshifyCutoutMipped(wrapped))
}

class StandardLilypadModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {
    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.lilypad.enabled) return

        val random = randomSupplier.get()
        ShadersModIntegration.grass(context, BetterFoliage.config.lilypad.shaderWind) {
            context.meshConsumer().accept(lilypadRootModels[random])
        }
        if (random.nextInt(64) < BetterFoliage.config.lilypad.population) {
            context.meshConsumer().accept(lilypadFlowerModels[random])
        }
    }

    companion object {
        val lilypadRootSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_lilypad_roots_$idx")
        }
        val lilypadFlowerSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_lilypad_flower_$idx")
        }
        val lilypadRootModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = tuftShapeSet(1.0, 1.0, 1.0, BetterFoliage.config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white.asInt) { lilypadRootSprites[it] }
                .transform { move(2.0 to DOWN) }
                .buildTufts()
        }
        val lilypadFlowerModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = tuftShapeSet(0.5, 0.5, 0.5, BetterFoliage.config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white.asInt) { lilypadFlowerSprites[it] }
                .transform { move(1.0 to DOWN) }
                .buildTufts()
        }
    }
}