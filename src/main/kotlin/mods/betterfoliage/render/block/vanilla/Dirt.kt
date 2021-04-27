package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.render.DIRT_BLOCKS
import mods.betterfoliage.render.SALTWATER_BIOMES
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.reedLighting
import mods.betterfoliage.render.lighting.renderMasquerade
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.resource.discovery.BlockRenderKey
import mods.betterfoliage.resource.discovery.ModelDiscoveryBase
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.generated.CenteredSprite
import mods.betterfoliage.resource.model.*
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockRenderLayer
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.UP
import net.minecraft.world.ExtendedBlockView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object DirtKey : BlockRenderKey {
    override fun replace(model: BakedModel, state: BlockState) = DirtModel(meshifyStandard(model, state))
}

object DirtDiscovery : ModelDiscoveryBase() {
    override val logger = BetterFoliage.logDetail

    override fun processModel(ctx: ModelDiscoveryContext, atlas: Consumer<Identifier>) =
        if (ctx.state.block in DIRT_BLOCKS) DirtKey else null
}

class DirtModel(wrapped: BakedModel) : WrappedBakedModel(wrapped) {

    val algaeLighting = grassTuftLighting(UP)
    val reedLighting = reedLighting()

    override fun emitBlockQuads(blockView: ExtendedBlockView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        if (!BetterFoliage.config.enabled) return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)

        val ctx = BasicBlockCtx(blockView, pos)
        val stateUp = ctx.offset(UP).state
        val keyUp = BetterFoliage.modelReplacer[stateUp]

        val isWater = stateUp.material == Material.WATER
        val isDeepWater = isWater && ctx.offset(Int3(2 to UP)).state.material == Material.WATER
        val isShallowWater = isWater && ctx.offset(Int3(2 to UP)).state.isAir
        val isSaltWater = isWater && ctx.biome.category in SALTWATER_BIOMES

        if (BetterFoliage.config.connectedGrass.enabled && keyUp is GrassKey) {
            val grassBaseModel = (ctx.model(UP) as WrappedBakedModel).wrapped
            context.renderMasquerade(grassBaseModel, blockView, stateUp, pos, randomSupplier, context)
        } else {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }

        val random = randomSupplier.get()
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
        val algaeModels by LazyInvalidatable(BetterFoliage.modelReplacer) {
            val shapes = BetterFoliage.config.algae.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { algaeSprites[randomI()] }
                .withOpposites()
                .build(BlockRenderLayer.CUTOUT_MIPPED, flatLighting = false)

        }
        val reedModels by LazyInvalidatable(BetterFoliage.modelReplacer) {
            val shapes = BetterFoliage.config.reed.let { tuftShapeSet(2.0, it.heightMin, it.heightMax, it.hOffset) }
            tuftModelSet(shapes, Color.white.asInt) { reedSprites[randomI()] }
                .withOpposites()
                .build(BlockRenderLayer.CUTOUT_MIPPED, flatLighting = false)
        }
    }
}