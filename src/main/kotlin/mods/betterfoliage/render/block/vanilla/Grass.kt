package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.render.SNOW_MATERIALS
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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.*
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

interface GrassKey : BlockRenderKey {
    val grassTopTexture: Identifier

    /**
     * Color to use for Short Grass rendering instead of the biome color.
     *
     * Value is null if the texture is mostly grey (the saturation of its average color is under a configurable limit),
     * the average color of the texture otherwise.
     */
    val overrideColor: Int?
}

object StandardGrassDiscovery : ConfigurableModelDiscovery() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BetterFoliage.blockConfig.grassBlocks
    override val modelTextures: List<ModelTextureList> get() = BetterFoliage.blockConfig.grassModels.modelList

    override fun processModel(state: BlockState, textures: List<Identifier>, atlas: Consumer<Identifier>): BlockRenderKey? {
        val grassId = textures[0]
        log(" block state $state")
        log("       texture $grassId")
        return GrassBlockModel.Key(grassId, getAndLogColorOverride(grassId, Atlas.BLOCKS, BetterFoliage.config.shortGrass.saturationThreshold))
    }
}

class GrassBlockModel(val key: Key, wrapped: BakedModel) : WrappedBakedModel(wrapped), FabricBakedModel {

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
            (!isSnowed || BetterFoliage.config.connectedGrass.snowEnabled) && (
                BetterFoliage.modelReplacer[stateBelow].let { it is DirtKey || it is GrassKey }
            )

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

    data class Key(
        override val grassTopTexture: Identifier,
        override val overrideColor: Int?
    ) : GrassKey {
        override fun replace(model: BakedModel, state: BlockState) = GrassBlockModel(this, meshifyStandard(model, state))
    }

    companion object {
        val grassTuftSpritesNormal by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftSpritesSnowed by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            Identifier(BetterFoliage.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftShapes = LazyMap(BetterFoliage.modelReplacer) { key: GrassKey ->
            BetterFoliage.config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
        }
        val grassTuftMeshesNormal = LazyMap(BetterFoliage.modelReplacer) { key: GrassKey ->
            tuftModelSet(grassTuftShapes[key], key.overrideColor) { idx -> grassTuftSpritesNormal[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
        val grassTuftMeshesSnowed = LazyMap(BetterFoliage.modelReplacer) { key: GrassKey ->
            tuftModelSet(grassTuftShapes[key], Color.white.asInt) { idx -> grassTuftSpritesSnowed[randomI()] }
                .withOpposites()
                .build(BlendMode.CUTOUT_MIPPED, flatLighting = false)
        }
        val grassFullBlockMeshes = LazyMap(BetterFoliage.modelReplacer) { key: GrassKey ->
            Array(64) { fullCubeTextured(key.grassTopTexture, key.overrideColor) }
        }
        val snowFullBlockMeshes by LazyInvalidatable(BetterFoliage.modelReplacer) {
            Array(64) { fullCubeTextured(Identifier("block/snow"), Color.white.asInt) }
        }
    }
}
