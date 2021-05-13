package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.render.lighting.RoundLeafLightingPreferUp
import mods.betterfoliage.render.particle.LeafBlockModel
import mods.betterfoliage.render.particle.LeafParticleKey
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableBlockMatcher
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelDiscoveryContext
import mods.betterfoliage.resource.discovery.ModelTextureList
import mods.betterfoliage.resource.generated.GeneratedLeafSprite
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.colorOverride
import mods.betterfoliage.util.isSnow
import mods.betterfoliage.util.logColorOverride
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.INFO

object StandardLeafDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.leafModels.modelList

    override fun processModel(ctx: ModelDiscoveryContext, textureMatch: List<ResourceLocation>) {
        val leafType = LeafParticleRegistry.typeMappings.getType(textureMatch[0]) ?: "default"
        val generated = GeneratedLeafSprite(textureMatch[0], leafType)
            .register(BetterFoliage.generatedPack)
            .apply { ctx.sprites.add(this) }

        detailLogger.log(INFO, "     particle $leafType")
        ctx.addReplacement(StandardLeafKey(generated, leafType, null))
    }
}

data class StandardLeafKey(
    val roundLeafTexture: ResourceLocation,
    override val leafType: String,
    override val overrideColor: Color?
) : HalfBakedWrapperKey(), LeafParticleKey {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel): SpecialRenderModel {
        val leafSpriteColor = Atlas.BLOCKS[roundLeafTexture].averageColor.let { hsb ->
            logColorOverride(BetterFoliageMod.detailLogger(this), Config.leaves.saturationThreshold, hsb)
            hsb.colorOverride(Config.leaves.saturationThreshold)
        }
        return StandardLeafModel(wrapped, this.copy(overrideColor = leafSpriteColor))
    }
}

class StandardLeafModel(
    model: SpecialRenderModel,
    override val key: StandardLeafKey
) : HalfBakedSpecialWrapper(model), LeafBlockModel {
    val leafNormal by leafModelsNormal.delegate(key)
    val leafSnowed by leafModelsSnowed.delegate(key)

    override fun render(ctx: RenderCtxBase, noDecorations: Boolean) {
        super.render(ctx, noDecorations)
        if (!Config.enabled || !Config.leaves.enabled || noDecorations) return

        ctx.vertexLighter = RoundLeafLightingPreferUp
        val leafIdx = ctx.random.nextInt(64)
        ctx.renderQuads(leafNormal[leafIdx])
        if (ctx.state(UP).isSnow) ctx.renderQuads(leafSnowed[leafIdx])
    }

    companion object {
        val leafSpritesSnowed by SpriteSetDelegate(Atlas.BLOCKS) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "blocks/better_leaves_snowed_$idx")
        }
        val leafModelsBase = LazyMapInvalidatable(BakeWrapperManager) { key: StandardLeafKey ->
            Config.leaves.let { crossModelsRaw(64, it.size, it.hOffset, it.vOffset) }
        }
        val leafModelsNormal = LazyMapInvalidatable(BakeWrapperManager) { key: StandardLeafKey ->
            crossModelsTextured(leafModelsBase[key], key.tintIndex, true) { key.roundLeafTexture }
        }
        val leafModelsSnowed = LazyMapInvalidatable(BakeWrapperManager) { key: StandardLeafKey ->
            crossModelsTextured(leafModelsBase[key], -1, false) { leafSpritesSnowed[it].name }
        }
    }
}