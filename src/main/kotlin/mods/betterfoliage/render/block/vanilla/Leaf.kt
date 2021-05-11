package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.Client
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.ConfigurableBlockMatcher
import mods.betterfoliage.config.ModelTextureList
import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HSB
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.HalfBakedWrapperKey
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.model.crossModelsRaw
import mods.betterfoliage.model.crossModelsTextured
import mods.betterfoliage.render.lighting.RoundLeafLighting
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableModelDiscovery
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.resource.generated.GeneratedLeaf
import mods.betterfoliage.texture.LeafBlockModel
import mods.betterfoliage.texture.LeafParticleKey
import mods.betterfoliage.texture.LeafParticleRegistry
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.isSnow
import net.minecraft.block.BlockState
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.DEBUG
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Logger

object StandardLeafDiscovery : ConfigurableModelDiscovery() {
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.leafModels.modelList

    override fun processModel(
        state: BlockState,
        location: ResourceLocation,
        textureMatch: List<ResourceLocation>,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakingKey>
    ): Boolean {
        val leafType = LeafParticleRegistry.typeMappings.getType(textureMatch[0]) ?: "default"
        val generated = GeneratedLeaf(textureMatch[0], leafType)
            .register(Client.generatedPack)
            .apply { sprites.add(this) }

        detailLogger.log(INFO, "     particle $leafType")
        replacements[location] = StandardLeafKey(generated, leafType, null)
        return true
    }
}

fun logColorOverride(logger: Logger, threshold: Double, hsb: HSB) {
    return if (hsb.saturation >= threshold) {
        logger.log(INFO, "         brightness ${hsb.brightness}")
        logger.log(INFO, "         saturation ${hsb.saturation} >= ${threshold}, will use texture color")
    } else {
        logger.log(INFO, "         saturation ${hsb.saturation} < ${threshold}, will use block color")
    }
}

fun HSB.colorOverride(threshold: Double) =
    if (saturation < threshold) null else copy(brightness = (brightness * 2.0f).coerceAtMost(0.9f)).asColor.let { Color(it) }

data class StandardLeafKey(
    val roundLeafTexture: ResourceLocation,
    override val leafType: String,
    override val overrideColor: Color?
) : HalfBakedWrapperKey(), LeafParticleKey {
    val tintIndex: Int get() = if (overrideColor == null) 0 else -1

    override fun replace(wrapped: SpecialRenderModel): SpecialRenderModel {
        val leafSpriteColor = Atlas.BLOCKS[roundLeafTexture].averageColor.let { hsb ->
            logColorOverride(BetterFoliageMod.detailLogger(this), Config.leaves.saturationThreshold, hsb)
            hsb.colorOverride(Config.leaves.saturationThreshold)
        }
        detailLogger.log(DEBUG, "roundLeaf=$roundLeafTexture overrideColor=$leafSpriteColor")
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

        (ctx as? RenderCtxVanilla)?.let { it.vertexLighter = RoundLeafLighting }
        ctx.render(leafNormal[ctx.random.nextInt(64)])
        if (ctx.state(UP).isSnow) ctx.render(leafSnowed[ctx.random.nextInt(64)])
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