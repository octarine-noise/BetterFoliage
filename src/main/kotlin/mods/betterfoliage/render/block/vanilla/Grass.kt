package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.SNOW_MATERIALS
import mods.betterfoliage.render.ShadersModIntegration
import mods.betterfoliage.render.lighting.grassTuftLighting
import mods.betterfoliage.render.lighting.withLighting
import mods.betterfoliage.resource.discovery.*
import mods.betterfoliage.model.*
import mods.betterfoliage.util.*
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BasicBakedModel
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.*
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

object StandardGrassDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.grassBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.grassModels.modelList

    override fun processModel(ctx: ModelDiscoveryContext, textureMatch: List<Identifier>) {
        ctx.addReplacement(StandardGrassKey(textureMatch[0], null))
        BetterFoliage.blockTypes.grass.add(ctx.blockState)
    }
}

data class StandardGrassKey(
    val grassLocation: Identifier,
    val overrideColor: Color?
) : ModelWrapKey() {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun bake(ctx: ModelBakingContext, wrapped: BasicBakedModel): BakedModel {
        val grassSpriteColor = Atlas.BLOCKS[grassLocation].averageColor.let { hsb ->
            logColorOverride(detailLogger, BetterFoliage.config.shortGrass.saturationThreshold, hsb)
            hsb.colorOverride(BetterFoliage.config.shortGrass.saturationThreshold)
        }
        return StandardGrassModel(meshifyCutoutMipped(wrapped), this.copy(overrideColor = grassSpriteColor))
    }
}

class StandardGrassModel(wrapped: BakedModel, val key: StandardGrassKey) : WrappedBakedModel(wrapped) {

    val tuftNormal by grassTuftMeshesNormal.delegate(key)
    val tuftSnowed by grassTuftMeshesSnowed.delegate(key)
    val fullBlock by grassFullBlockMeshes.delegate(key)

    val tuftLighting = grassTuftLighting(UP)

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        if (!BetterFoliage.config.enabled) return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)

        val ctx = BasicBlockCtx(blockView, pos)
        val stateBelow = ctx.state(DOWN)
        val stateAbove = ctx.state(UP)

        val isSnowed = stateAbove.material in SNOW_MATERIALS
        val connected = BetterFoliage.config.connectedGrass.enabled &&
            (!isSnowed || BetterFoliage.config.connectedGrass.snowEnabled) &&
            (stateBelow in BetterFoliage.blockTypes.dirt || stateBelow in BetterFoliage.blockTypes.grass)

        val random = randomSupplier.get()
        if (connected) {
            context.meshConsumer().accept(if (isSnowed) snowFullBlockMeshes[random] else fullBlock[random])
        } else {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }

        if (BetterFoliage.config.shortGrass.enabled(random) && !ctx.isNeighborSolid(UP)) {
            ShadersModIntegration.grass(context, BetterFoliage.config.shortGrass.shaderWind) {
                context.withLighting(tuftLighting) {
                    it.accept(if (isSnowed) tuftSnowed[random] else tuftNormal[random])
                }
            }
        }
    }

    companion object {
        val grassTuftSpritesNormal by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftSpritesSnowed by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftShapes = LazyMap(BakeWrapperManager) { key: StandardGrassKey ->
            BetterFoliage.config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
        }
        val grassTuftMeshesNormal = LazyMap(BakeWrapperManager) { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes[key], key.tintIndex) { idx -> grassTuftSpritesNormal[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
        val grassTuftMeshesSnowed = LazyMap(BakeWrapperManager) { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes[key], Color.white.asInt) { idx -> grassTuftSpritesSnowed[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
        val grassFullBlockMeshes = LazyMap(BakeWrapperManager) { key: StandardGrassKey ->
            Array(64) { fullCubeTextured(key.grassLocation, key.tintIndex) }
        }
        val snowFullBlockMeshes by LazyInvalidatable(BakeWrapperManager) {
            Array(64) { fullCubeTextured(Identifier("block/snow"), Color.white.asInt) }
        }
    }
}
