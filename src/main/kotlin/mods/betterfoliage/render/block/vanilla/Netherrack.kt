package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.NETHERRACK_BLOCKS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.build
import mods.betterfoliage.model.meshifyCutoutMipped
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.model.withOpposites
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.get
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Supplier

object StandardNetherrackDiscovery : AbstractModelDiscovery() {

    fun canRenderInLayer(layer: RenderLayer) = when {
        !BetterFoliage.config.enabled -> layer == RenderLayer.getSolid()
        !BetterFoliage.config.netherrack.enabled -> layer == RenderLayer.getSolid()
        else -> layer == RenderLayer.getCutoutMipped()
    }

    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is JsonUnbakedModel && ctx.blockState.block in NETHERRACK_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardNetherrackKey)
//            RenderTypeLookup.setRenderLayer(ctx.blockState.block, ::canRenderInLayer)
        }
        super.processModel(ctx)
    }
}

object StandardNetherrackKey : ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = StandardNetherrackModel(meshifyCutoutMipped(wrapped))
}

class StandardNetherrackModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {

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
        val netherrackTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = BetterFoliage.config.netherrack.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { netherrackTuftSprites[randomI()] }
                .transform { rotate(Rotation.fromUp[DOWN.ordinal]).rotateUV(2) }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
    }
}