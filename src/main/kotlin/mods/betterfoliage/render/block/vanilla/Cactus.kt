package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.config.CACTUS_BLOCKS
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderData
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.model.transform
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.lighting.RoundLeafLighting
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ParametrizedModelDiscovery
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.horizontalDirections
import mods.betterfoliage.util.idx
import mods.betterfoliage.util.lazy
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.ResourceLocation
import java.util.Random

object StandardCactusDiscovery : ParametrizedModelDiscovery() {
    override fun processModel(ctx: ModelDiscoveryContext, params: Map<String, String>) {
        ctx.addReplacement(StandardCactusKey)
        ctx.sprites.add(StandardCactusModel.cactusCrossSprite)
    }
}

object StandardCactusKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardCactusModel(wrapped)
}

class CactusRenderData(val armSide: Int, val armIdx: Int, val crossIdx: Int)

class StandardCactusModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    override fun prepare(ctx: BlockCtx, random: Random): Any = when {
        !Config.enabled || !Config.cactus.enabled -> Unit
        else -> CactusRenderData(
            armSide = random.nextInt() and 3,
            armIdx = random.idx(cactusArmModels),
            crossIdx = random.idx(cactusCrossModels)
        )
    }

    val armLighting = horizontalDirections.map { LightingPreferredFace(it) }.toTypedArray()

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        super.renderLayer(ctx, data, layer)
        if (data is CactusRenderData) {
            ctx.vertexLighter = armLighting[data.armSide]
            ctx.renderQuads(cactusArmModels[data.armSide][data.armIdx])
            ctx.vertexLighter = RoundLeafLighting
            ctx.renderQuads(cactusCrossModels[data.crossIdx])
        }
    }

    companion object {
        val cactusCrossSprite = ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_cactus")
        val cactusArmSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_cactus_arm_$idx")
        }
        val cactusArmModels by BetterFoliage.modelManager.lazy {
            val shapes = Config.cactus.let { tuftShapeSet(0.8, 0.8, 0.8, 0.2) }
            val models = tuftModelSet(shapes, Color.white) { cactusArmSprites[randomI()] }
            horizontalDirections.map { side ->
                models.transform { move(0.0625 to DOWN).rotate(Rotation.fromUp[side.ordinal]) }.buildTufts()
            }.toTypedArray()
        }
        val cactusCrossModels by BetterFoliage.modelManager.lazy {
            val models = Config.cactus.let { config ->
                crossModelsRaw(64, config.size, 0.0, 0.0)
                    .transform { rotateZ(randomD(-config.sizeVariation, config.sizeVariation)) }
            }
            crossModelsTextured(models, -1, true) { cactusCrossSprite }
        }
    }
}