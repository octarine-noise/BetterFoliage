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
import mods.betterfoliage.util.averageHSB
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.lazyMap
import mods.betterfoliage.util.lighten
import mods.betterfoliage.util.randomI
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardMyceliumDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        val texture = params.location("texture") ?: return
        val tint = params.int("tint") ?: -1
        val color = Atlas.BLOCKS.file(texture).averageHSB.lighten(multiplier = 1.5f)
        ctx.addReplacement(StandardMyceliumKey(texture, tint, color))
        ctx.blockState.block.extendLayers()
    }
}

data class StandardMyceliumKey(
    val sprite: ResourceLocation,
    val tintIndex: Int,
    val avgColor: Color,
) : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel): SpecialRenderModel {
        return StandardMyceliumModel(wrapped, this)
    }
}

class MyceliumRenderData(
    val tuftIndex: Int?
) : SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = tuftIndex != null && layer == Layers.tufts
}

class StandardMyceliumModel(
    wrapped: SpecialRenderModel,
    key: StandardMyceliumKey
) : HalfBakedSpecialWrapper(wrapped) {

    val tuftModels by myceliumTuftModels.delegate(key)
    val tuftLighting = LightingPreferredFace(Direction.UP)

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit
        return MyceliumRenderData(
            random.idxOrNull(tuftModels) {
                Config.shortGrass.enabled(random) &&
                Config.shortGrass.myceliumEnabled &&
                ctx.state(Direction.UP).isAir(ctx.world, ctx.pos)
            }
        )
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        super.renderLayer(ctx, data, layer)
        if (data is MyceliumRenderData && data.tuftIndex != null && layer == Layers.tufts) {
            ctx.vertexLighter = tuftLighting
            ctx.renderQuads(tuftModels[data.tuftIndex])
        }
    }

    companion object {
        val myceliumTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_mycel_$idx")
        }
        val myceliumTuftShapes by BetterFoliage.modelManager.lazy {
            Config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
        }
        val myceliumTuftModels = BetterFoliage.modelManager.lazyMap { key: StandardMyceliumKey ->
            tuftModelSet(myceliumTuftShapes, key.avgColor, key.tintIndex) { idx -> myceliumTuftSprites[randomI()] }.buildTufts()
        }
    }
}