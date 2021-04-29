package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.BlockRenderKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryBase
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.model.*
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object NetherrackKey : BlockRenderKey {
    override fun replace(model: BakedModel, state: BlockState) = NetherrackModel(meshifyStandard(model, state))
}

object NetherrackDiscovery : ModelDiscoveryBase() {
    override val logger = BetterFoliage.logDetail
    val netherrackBlocks = listOf(Blocks.NETHERRACK)
    override fun processModel(ctx: ModelDiscoveryContext, atlas: Consumer<Identifier>) =
        if (ctx.state.block in netherrackBlocks) NetherrackKey else null
}

class NetherrackModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {

    val tuftLighting = grassTuftLighting(DOWN)

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        if (BetterFoliage.config.enabled &&
            BetterFoliage.config.netherrack.enabled &&
            blockView.getBlockState(pos + DOWN.offset).isAir
        ) {
            val random = randomSupplier.get()
            context.withLighting(tuftLighting) {
                it.accept(netherrackTuftModels[random])
            }
        }
    }

    companion object {
        val netherrackTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_netherrack_$idx")
        }
        val netherrackTuftModels by LazyInvalidatable(BetterFoliage.modelReplacer) {
            val shapes = BetterFoliage.config.netherrack.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { netherrackTuftSprites[randomI()] }
                .transform { rotate(Rotation.fromUp[DOWN.ordinal]).rotateUV(2) }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
    }
}