package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.Color
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
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.randomI
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardNetherrackDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        ctx.addReplacement(StandardNetherrackKey)
        ctx.blockState.block.extendLayers()
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
        val netherrackTuftModels by BetterFoliage.modelManager.lazy {
            val shapes = Config.netherrack.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white, -1) { netherrackTuftSprites[randomI()] }
                .transform { rotate(Rotation.fromUp[DOWN.ordinal]).rotateUV(2) }
                .buildTufts()
        }
    }
}