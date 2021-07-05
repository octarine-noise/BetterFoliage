package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.NETHERRACK_BLOCKS
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderData
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.Layers
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.extendLayers
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.get
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.randomI
import net.minecraft.block.Blocks
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardNetherrackDiscovery : AbstractModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is BlockModel && ctx.blockState.block in NETHERRACK_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardNetherrackKey)
            ctx.blockState.block.extendLayers()
        }
        super.processModel(ctx)
    }
}

object StandardNetherrackKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardNetherrackModel(wrapped)
}

class NetherrackRenderData(
    val tuftIndex: Int?
): SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = tuftIndex != null && layer == Layers.tufts
}

class StandardNetherrackModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {

    val tuftLighting = LightingPreferredFace(DOWN)

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit
        return NetherrackRenderData(
            random.idxOrNull(netherrackTuftModels) {
                Config.netherrack.enabled &&
                ctx.isAir(DOWN)
            }
        )
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        super.renderLayer(ctx, data, layer)
        if (data is NetherrackRenderData && data.tuftIndex != null && layer == Layers.tufts) {
            ctx.vertexLighter = tuftLighting
            ctx.renderQuads(netherrackTuftModels[data.tuftIndex])
        }
    }

    companion object {
        val netherrackTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_netherrack_$idx")
        }
        val netherrackTuftModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.netherrack.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, -1) { netherrackTuftSprites[randomI()] }
                .transform { rotate(Rotation.fromUp[DOWN.ordinal]).rotateUV(2) }
                .buildTufts()
        }
    }
}