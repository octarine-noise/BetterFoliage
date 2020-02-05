package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.resource.discovery.BlockRenderKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryBase
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.RenderKeyFactory
import mods.betterfoliage.resource.model.*
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.semiRandom
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.ExtendedBlockView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object LilypadKey : BlockRenderKey {
    override fun replace(model: BakedModel, state: BlockState) = LilypadModel(meshifyStandard(model, state))
}

object LilyPadDiscovery : ModelDiscoveryBase() {
    override val logger = BetterFoliage.logDetail
    override fun processModel(ctx: ModelDiscoveryContext, atlas: Consumer<Identifier>) =
        if (ctx.state.block == Blocks.LILY_PAD) LilypadKey else null
}

class LilypadModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {
    override fun emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (!BetterFoliage.config.enabled || !BetterFoliage.config.lilypad.enabled) return

        val random = randomSupplier.get()
        context.meshConsumer().accept(lilypadRootModels[random])
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
        val lilypadRootModels by LazyInvalidatable(BetterFoliage.modelReplacer) {
            val shapes = tuftShapeSet(1.0, 1.0, 1.0, BetterFoliage.config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white.asInt) { lilypadRootSprites[it] }
                .transform { move(2.0 to DOWN) }
                .buildTufts()
        }
        val lilypadFlowerModels by LazyInvalidatable(BetterFoliage.modelReplacer) {
            val shapes = tuftShapeSet(0.5, 0.5, 0.5, BetterFoliage.config.lilypad.hOffset)
            tuftModelSet(shapes, Color.white.asInt) { lilypadFlowerSprites[it] }
                .transform { move(1.0 to DOWN) }
                .buildTufts()
        }
    }
}