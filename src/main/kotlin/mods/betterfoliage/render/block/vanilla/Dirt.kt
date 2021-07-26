package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.SALTWATER_BIOMES
import mods.betterfoliage.config.isSnow
import mods.betterfoliage.integration.ShadersModIntegration
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
import mods.betterfoliage.resource.generated.CenteredSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.getBlockModel
import mods.betterfoliage.util.idxOrNull
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.randomI
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardDirtDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        BetterFoliage.blockTypes.dirt.add(ctx.blockState)
        ctx.addReplacement(StandardDirtKey)
        ctx.blockState.block.extendLayers()
    }
}

object StandardDirtKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardDirtModel(wrapped)
}

class DirtRenderData(
    val connectedGrassModel: SpecialRenderModel?,
    val algaeIdx: Int?,
    val reedIdx: Int?
) : SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = when {
        connectedGrassModel != null && layer == Layers.connectedDirt -> true
        (algaeIdx != null || reedIdx != null) && layer == Layers.tufts -> true
        else -> false
    }
}

class StandardDirtModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    val vanillaTuftLighting = LightingPreferredFace(UP)

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit
        val stateUp = ctx.state(UP)
        val state2Up = ctx.state(Int3(0, 2, 0))
        val isConnectedGrass = Config.connectedGrass.enabled &&
                stateUp in BetterFoliage.blockTypes.grass &&
                (Config.connectedGrass.snowEnabled || !state2Up.isSnow)

        val isWater = stateUp.material == Material.WATER
        val isDeepWater = isWater && state2Up.material == Material.WATER
        val isShallowWater = isWater && state2Up.isAir
        val isSaltWater = isWater && ctx.biome?.biomeCategory in SALTWATER_BIOMES

        // get the actual grass model to use for connected grass rendering
        // return null if the grass specifically does not want to connect
        val connectedGrassModel = if (!isConnectedGrass) null else getBlockModel(stateUp).let { model ->
            (model as? SpecialRenderModel)?.resolve(random)?.let { grassModel ->
                if ((grassModel as? StandardGrassModel)?.key?.noConnect == true) null else grassModel
            }
        }

        return DirtRenderData(
            connectedGrassModel = connectedGrassModel,
            algaeIdx = random.idxOrNull(algaeModels) { Config.algae.enabled(random) && isDeepWater && isSaltWater },
            reedIdx = random.idxOrNull(reedModels) { Config.reed.enabled(random) && isShallowWater && !isSaltWater }
        )
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        if (data is DirtRenderData) {
            if (data.connectedGrassModel != null) {
                ctx.renderMasquerade(UP.offset) {
                    data.connectedGrassModel.renderLayer(ctx, ctx.state(UP), layer)
                }
            } else {
                // render non-connected grass
                super.renderLayer(ctx, data, layer)
            }

            if (layer == Layers.tufts) {
                data.algaeIdx?.let {
                    ctx.vertexLighter = vanillaTuftLighting
                    ShadersModIntegration.grass(ctx, Config.algae.shaderWind) {
                        ctx.renderQuads(algaeModels[it])
                    }
                }
                data.reedIdx?.let {
                    ctx.vertexLighter = vanillaTuftLighting
                    ShadersModIntegration.grass(ctx, Config.reed.shaderWind) {
                        ctx.renderQuads(reedModels[it])
                    }
                }
            }
        } else super.renderLayer(ctx, data, layer)
    }

    companion object {
        val algaeSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_algae_$idx")
        }
        val reedSprites by SpriteSetDelegate(
            Atlas.BLOCKS,
            idFunc = { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_reed_$idx") },
            idRegister = { id -> CenteredSprite(id, aspectHeight = 2).register(BetterFoliage.generatedPack) }
        )
        val algaeModels by BetterFoliage.modelManager.lazy {
            val shapes = Config.algae.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white, -1) { algaeSprites[randomI()] }.buildTufts()
        }
        val reedModels by BetterFoliage.modelManager.lazy {
            val shapes = Config.reed.let { tuftShapeSet(2.0, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white, -1) { reedSprites[randomI()] }.buildTufts()
        }
    }
}