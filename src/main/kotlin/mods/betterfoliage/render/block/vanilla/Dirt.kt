package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.DIRT_BLOCKS
import mods.betterfoliage.config.SALTWATER_BIOMES
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.generated.CenteredSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.randomI
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome

object StandardDirtDiscovery : AbstractModelDiscovery() {
    fun canRenderInLayer(layer: RenderType) = when {
        !Config.enabled -> layer == RenderType.getSolid()
        !Config.connectedGrass.enabled && !Config.algae.enabled && !Config.reed.enabled -> layer == RenderType.getSolid()
        else -> layer == RenderType.getCutoutMipped()
    }

    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is BlockModel && ctx.blockState.block in DIRT_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardDirtKey)
            RenderTypeLookup.setRenderLayer(ctx.blockState.block, ::canRenderInLayer)
        }
        super.processModel(ctx)
    }
}

object StandardDirtKey : HalfBakedWrapperKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel) = StandardDirtModel(wrapped)
}

class StandardDirtModel(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {
    val vanillaTuftLighting = LightingPreferredFace(UP)

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        if (!Config.enabled || noDecorations) return super.render(ctx, noDecorations)

        val stateUp = ctx.state(UP)
        val state2Up = ctx.state(Int3(0, 2, 0))
        val isConnectedGrass = Config.connectedGrass.enabled && stateUp in BetterFoliage.blockTypes.grass
        if (isConnectedGrass) {
            (ctx.blockModelShapes.getModel(stateUp) as? SpecialRenderModel)?.let { grassModel ->
                ctx.renderMasquerade(UP.offset) {
                    grassModel.render(ctx, true)
                }
                return
            }
            return super.render(ctx, false)
        }

        super.render(ctx, false)

        val isWater = stateUp.material == Material.WATER
        val isDeepWater = isWater && ctx.offset(Int3(2 to UP)).state.material == Material.WATER
        val isShallowWater = isWater && ctx.offset(Int3(2 to UP)).state.isAir
        val isSaltWater = isWater && ctx.biome?.category in SALTWATER_BIOMES

        if (Config.algae.enabled(ctx.random) && isDeepWater) {
            ctx.vertexLighter = vanillaTuftLighting
            ShadersModIntegration.grass(ctx, Config.algae.shaderWind) {
                ctx.renderQuads(algaeModels[ctx.random])
            }
        } else if (Config.reed.enabled(ctx.random) && isShallowWater && !isSaltWater) {
            ctx.vertexLighter = vanillaTuftLighting
            ShadersModIntegration.grass(ctx, Config.reed.shaderWind) {
                ctx.renderQuads(reedModels[ctx.random])
            }
        }
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
        val algaeModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.algae.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, -1) { algaeSprites[randomI()] }.buildTufts()
        }
        val reedModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = Config.reed.let { tuftShapeSet(2.0, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, -1) { reedSprites[randomI()] }.buildTufts()
        }
    }
}