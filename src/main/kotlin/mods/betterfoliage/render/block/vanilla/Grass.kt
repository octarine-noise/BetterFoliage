package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.BlockConfigOld
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.isSnow
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderData
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.fullCubeTextured
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.Layers
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.extendLayers
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.colorOverride
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.lazyMap
import mods.betterfoliage.util.logColorOverride
import mods.betterfoliage.util.randomI
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardGrassDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        val textureGrass = params.texture("texture-grass") ?: return
        ctx.addReplacement(StandardGrassKey(textureGrass, null))
        BetterFoliage.blockTypes.grass.add(ctx.blockState)
        ctx.blockState.block.extendLayers()
    }
}

data class StandardGrassKey(
    val grassLocation: ResourceLocation,
    val overrideColor: Color?
) : HalfBakedWrapperKey() {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel): SpecialRenderModel {
        val grassSpriteColor = Atlas.BLOCKS[grassLocation].averageColor.let { hsb ->
            logColorOverride(BetterFoliageMod.detailLogger(this), Config.shortGrass.saturationThreshold, hsb)
            hsb.colorOverride(Config.shortGrass.saturationThreshold)
        }
        return StandardGrassModel(wrapped, this.copy(overrideColor = grassSpriteColor))
    }
}

class GrassRenderData(
    val isSnowed: Boolean,
    val connectedGrassIdx: Int?,
    val tuftIdx: Int?
): SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = when {
        connectedGrassIdx != null && layer == Layers.connectedGrass -> true
        tuftIdx != null && layer == Layers.tufts -> true
        else -> false
    }
}

class StandardGrassModel(
    wrapped: SpecialRenderModel,
    key: StandardGrassKey
) : HalfBakedSpecialWrapper(wrapped) {

    val tuftNormal by grassTuftMeshesNormal.delegate(key)
    val tuftSnowed by grassTuftMeshesSnowed.delegate(key)
    val fullBlock by grassFullBlockMeshes.delegate(key)
    val tuftLighting = LightingPreferredFace(UP)

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit

        val stateBelow = ctx.state(DOWN)
        val stateAbove = ctx.state(UP)
        val isAir = ctx.isAir(UP)
        val isSnowed = stateAbove.isSnow
        val connected = Config.connectedGrass.enabled &&
                (!isSnowed || Config.connectedGrass.snowEnabled) &&
                BetterFoliage.blockTypes.run { stateBelow in grass || stateBelow in dirt }

        return GrassRenderData(
            isSnowed = isSnowed,
            connectedGrassIdx = random.idxOrNull(if (isSnowed) snowFullBlockMeshes else fullBlock) { connected },
            tuftIdx = random.idxOrNull(if (isSnowed) tuftSnowed else tuftNormal) {
                Config.shortGrass.enabled(random) &&
                        Config.shortGrass.grassEnabled &&
                        (isAir || isSnowed)
            }
        )
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        if (data is GrassRenderData) {
            if (data.connectedGrassIdx != null) {
                if (layer == Layers.connectedGrass)
                    ctx.renderQuads((if (data.isSnowed) snowFullBlockMeshes else fullBlock)[data.connectedGrassIdx])
            } else {
                super.renderLayer(ctx, data, layer)
            }
            if (data.tuftIdx != null && layer == Layers.tufts) {
                ctx.vertexLighter = tuftLighting
                ShadersModIntegration.grass(ctx, Config.shortGrass.shaderWind) {
                    ctx.renderQuads((if (data.isSnowed) tuftSnowed else tuftNormal)[data.tuftIdx])
                }
            }
        } else super.renderLayer(ctx, data, layer)
    }

    companion object {
        val grassTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftShapes by BetterFoliage.modelManager.lazy {
            Config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
        }
        val grassTuftMeshesNormal = BetterFoliage.modelManager.lazyMap { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes, key.overrideColor) { idx -> grassTuftSprites[randomI()] }.buildTufts()
        }
        val grassTuftMeshesSnowed = BetterFoliage.modelManager.lazyMap { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes, null) { idx -> grassTuftSprites[randomI()] }.buildTufts()
        }
        val grassFullBlockMeshes = BetterFoliage.modelManager.lazyMap { key: StandardGrassKey ->
            Array(64) { fullCubeTextured(key.grassLocation, key.tintIndex) }
        }
        val snowFullBlockMeshes by BetterFoliage.modelManager.lazy {
            Array(64) { fullCubeTextured(ResourceLocation("block/snow"), -1) }
        }
    }
}