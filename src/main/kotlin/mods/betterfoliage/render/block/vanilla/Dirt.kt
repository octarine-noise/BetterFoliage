package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.config.DIRT_BLOCKS
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.ModelWrapKey
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.WrappedBakedModel
import mods.betterfoliage.model.build
import mods.betterfoliage.model.meshifyStandard
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.model.withOpposites
import mods.betterfoliage.config.SALTWATER_BIOMES
import mods.betterfoliage.model.getUnderlyingModel
import mods.betterfoliage.model.meshifySolid
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.reedLighting
import mods.betterfoliage.render.lighting.renderMasquerade
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.AbstractModelDiscovery
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.generated.CenteredSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.get
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.UP
import net.minecraft.world.BlockRenderView
import java.util.Random
import java.util.function.Supplier

object StandardDirtDiscovery : AbstractModelDiscovery() {
    fun canRenderInLayer(layer: RenderLayer) = when {
        !BetterFoliage.config.enabled -> layer == RenderLayer.getSolid()
        (!BetterFoliage.config.connectedGrass.enabled &&
                !BetterFoliage.config.algae.enabled &&
                !BetterFoliage.config.reed.enabled
                ) -> layer == RenderLayer.getSolid()
        else -> layer == RenderLayer.getCutoutMipped()
    }

    override fun processModel(ctx: ModelDiscoveryContext) {
        if (ctx.getUnbaked() is JsonUnbakedModel && ctx.blockState.block in DIRT_BLOCKS) {
            BetterFoliage.blockTypes.dirt.add(ctx.blockState)
            ctx.addReplacement(StandardDirtKey)
//            RenderTypeLookup.setRenderLayer(ctx.blockState.block, ::canRenderInLayer)
        }
        super.processModel(ctx)
    }
}

object StandardDirtKey : ModelWrapKey() {
    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel) = DirtModel(meshifySolid(wrapped))
}

class DirtModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {

    val algaeLighting = grassTuftLighting(UP)
    val reedLighting = reedLighting()

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        if (!BetterFoliage.config.enabled) return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)

        val ctx = BasicBlockCtx(blockView, pos)
        val stateUp = ctx.offset(UP).state
        val isGrassUp = stateUp in BetterFoliage.blockTypes.grass

        val isWater = stateUp.material == Material.WATER
        val isDeepWater = isWater && ctx.offset(Int3(2 to UP)).state.material == Material.WATER
        val isShallowWater = isWater && ctx.offset(Int3(2 to UP)).state.isAir
        val isSaltWater = isWater && ctx.biome?.category in SALTWATER_BIOMES

        val random = randomSupplier.get()
        if (BetterFoliage.config.connectedGrass.enabled && isGrassUp) {
            val grassBaseModel = getUnderlyingModel(ctx.model(UP), random)
            context.renderMasquerade(grassBaseModel, blockView, stateUp, pos, randomSupplier, context)
        } else {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }

        if (BetterFoliage.config.algae.enabled(random) && isDeepWater) {
            ShadersModIntegration.grass(context, BetterFoliage.config.algae.shaderWind) {
                context.withLighting(algaeLighting) {
                    it.accept(algaeModels[random])
                }
            }
        } else if (BetterFoliage.config.reed.enabled(random) && isShallowWater && !isSaltWater) {
            ShadersModIntegration.grass(context, BetterFoliage.config.reed.shaderWind) {
                context.withLighting(reedLighting) {
                    it.accept(reedModels[random])
                }
            }
        }
    }

    companion object {
        val algaeSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_algae_$idx")
        }
        val reedSprites by SpriteSetDelegate(Atlas.BLOCKS,
            idFunc = { idx -> Identifier(BetterFoliage.MOD_ID, "blocks/better_reed_$idx") },
            idRegister = { id -> CenteredSprite(id, aspectHeight = 2).register(BetterFoliage.generatedPack) }
        )
        val algaeModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes =
                BetterFoliage.config.algae.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { algaeSprites[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)

        }
        val reedModels by LazyInvalidatable(BakeWrapperManager) {
            val shapes = BetterFoliage.config.reed.let { tuftShapeSet(2.0, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { reedSprites[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
    }
}