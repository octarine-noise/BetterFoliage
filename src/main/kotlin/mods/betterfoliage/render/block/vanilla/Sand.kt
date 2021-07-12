package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.SALTWATER_BIOMES
import mods.betterfoliage.config.SAND_BLOCKS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.Quad
import mods.betterfoliage.model.SpecialRenderData
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.bake
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
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.get
import mods.betterfoliage.util.idx
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.lazyMap
import mods.betterfoliage.util.mapArray
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardSandDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        ctx.addReplacement(StandardSandKey)
        ctx.blockState.block.extendLayers()
    }
}

object StandardSandKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardSandModel(wrapped)
}

class SandRenderData(
    val crustIdx: Array<Int?>,
    val tuftIdx: Array<Int?>
): SpecialRenderData {
    override fun canRenderInLayer(layer: RenderType) = when {
        (crustIdx.any { it != null } || tuftIdx.any { it != null }) && layer == Layers.coral -> true
        else -> false
    }
}

class StandardSandModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    val coralLighting = Direction.values().mapArray { LightingPreferredFace(it) }

    override fun prepare(ctx: BlockCtx, random: Random): Any {
        if (!Config.enabled) return Unit
        if (!Config.coral.enabled(random)) return Unit
        if (ctx.biome?.biomeCategory !in SALTWATER_BIOMES) return Unit

        val crustIdx = Array<Int?>(6) { null }
        val tuftIdx = Array<Int?>(6) { null }
        allDirections.filter { random.nextInt(64) < Config.coral.chance }.forEach { face ->
            val isWater = ctx.state(face).material == Material.WATER
            val isDeepWater = isWater && ctx.offset(face).state(UP).material == Material.WATER
            if (isDeepWater) {
                crustIdx[face.ordinal] = random.idx(coralCrustModels)
                tuftIdx[face.ordinal] = random.idx(coralTuftModels)
            }
        }
        return SandRenderData(crustIdx, tuftIdx)
    }

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        super.renderLayer(ctx, data, layer)
        if (data is SandRenderData && layer == Layers.coral) {
            for (face in 0 until 6) {
                ctx.vertexLighter = coralLighting[face]
                data.crustIdx[face]?.let { ctx.renderQuads(coralCrustModels[face][it]) }
                data.tuftIdx[face]?.let { ctx.renderQuads(coralTuftModels[face][it]) }
            }
        }
    }

    companion object {
        val coralTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_coral_$idx")
        }
        val coralCrustSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_crust_$idx")
        }
        val coralTuftModels by BetterFoliage.modelManager.lazy {
            val shapes = Config.coral.let { tuftShapeSet(it.size, 1.0, 1.0, it.hOffset) }
            allDirections.mapArray { face ->
                tuftModelSet(shapes, Color.white) { coralTuftSprites[randomI()] }
                    .transform { rotate(Rotation.fromUp[face]) }
                    .buildTufts()
            }
        }
        val coralCrustModels by BetterFoliage.modelManager.lazy {
            allDirections.map { face ->
                Array(64) { idx ->
                    listOf(
                        Quad.horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
                            .scale(Config.coral.crustSize)
                            .move(0.5 + randomD(0.01, Config.coral.vOffset) to UP)
                            .rotate(Rotation.fromUp[face])
                            .mirrorUV(randomB(), randomB()).rotateUV(randomI(max = 4))
                            .sprite(coralCrustSprites[idx]).colorAndIndex(null)
                    ).bake(applyDiffuseLighting = false)
                }
            }.toTypedArray()
        }
    }
}
