package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.Client
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.ConfigurableBlockMatcher
import mods.betterfoliage.config.ModelTextureList
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.buildTufts
import mods.betterfoliage.model.fullCubeTextured
import mods.betterfoliage.model.tuftModelSet
import mods.betterfoliage.model.tuftShapeSet
import mods.betterfoliage.render.lighting.LightingPreferredFace
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyInvalidatable
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.get
import mods.betterfoliage.util.isSnow
import mods.betterfoliage.util.randomI
import net.minecraft.block.BlockState
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation

object StandardGrassDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.grassBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.grassModels.modelList

    override fun processModel(
        state: BlockState,
        location: ResourceLocation,
        textureMatch: List<ResourceLocation>,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ): Boolean {
        replacements[location] = StandardGrassKey(textureMatch[0], null)
        Client.blockTypes.grass.add(state)
//        RenderTypeLookup.setRenderLayer(state.block, RenderType.getCutout())
        return true
    }
}

data class StandardGrassKey(
    val grassLocation: ResourceLocation,
    val overrideColor: Color?
) : HalfBakedWrapperKey() {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun replace(wrapped: SpecialRenderModel): SpecialRenderModel {
        val grassSpriteColor = Atlas.BLOCKS[grassLocation].averageColor.let { hsb ->
            logColorOverride(BetterFoliageMod.detailLogger(this), Config.shortGrass.saturationThreshold, hsb)
            hsb.colorOverride(Config.shortGrass.saturationThreshold)
        }
        return StandardGrassModel(wrapped, this.copy(overrideColor = grassSpriteColor))
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

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        if (!Config.enabled || noDecorations) return super.render(ctx, noDecorations)

        val stateBelow = ctx.state(DOWN)
        val stateAbove = ctx.state(UP)

        val isSnowed = stateAbove.isSnow
        val connected = Config.connectedGrass.enabled &&
                (!isSnowed || Config.connectedGrass.snowEnabled) &&
                Client.blockTypes.run { stateBelow in grass || stateBelow in dirt }

        if (connected) {
            ctx.render(if (isSnowed) snowFullBlockMeshes[ctx.random] else fullBlock[ctx.random])
        } else {
            super.render(ctx, noDecorations)
        }

        if (Config.shortGrass.enabled(ctx.random) && !ctx.isNeighborSolid(UP)) {
            (ctx as? RenderCtxVanilla)?.let { it.vertexLighter = tuftLighting }
            ctx.render(if (isSnowed) tuftSnowed[ctx.random] else tuftNormal[ctx.random])
        }
    }

    companion object {
        val grassTuftSprites by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_grass_long_$idx")
        }
        val grassTuftShapes by LazyInvalidatable(BakeWrapperManager) {
            Config.shortGrass.let { tuftShapeSet(it.size, it.heightMin, it.heightMax, it.hOffset) }
        }
        val grassTuftMeshesNormal = LazyMapInvalidatable(BakeWrapperManager) { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes, key.overrideColor) { idx -> grassTuftSprites[randomI()] }.buildTufts()
        }
        val grassTuftMeshesSnowed = LazyMapInvalidatable(BakeWrapperManager) { key: StandardGrassKey ->
            tuftModelSet(grassTuftShapes, Color.white) { idx -> grassTuftSprites[randomI()] }.buildTufts()
        }
        val grassFullBlockMeshes = LazyMapInvalidatable(BakeWrapperManager) { key: StandardGrassKey ->
            Array(64) { fullCubeTextured(key.grassLocation, key.tintIndex) }
        }
        val snowFullBlockMeshes by LazyInvalidatable(BakeWrapperManager) {
            Array(64) { fullCubeTextured(ResourceLocation("block/snow"), -1) }
        }
    }
}