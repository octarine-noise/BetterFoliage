package mods.betterfoliage.render.block.vanilla

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.Client
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.config.Config
import mods.betterfoliage.config.ConfigurableBlockMatcher
import mods.betterfoliage.config.ModelTextureList
import mods.betterfoliage.render.ISpecialRenderModel
import mods.betterfoliage.render.lighting.RoundLeafLighting
import mods.betterfoliage.render.old.Color
import mods.betterfoliage.render.old.HalfBakedSpecialWrapper
import mods.betterfoliage.render.old.HalfBakedWrapKey
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.RenderCtxVanilla
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.ConfigurableModelReplacer
import mods.betterfoliage.resource.discovery.ModelBakeKey
import mods.betterfoliage.resource.generated.GeneratedLeaf
import mods.betterfoliage.resource.model.SpriteSetDelegate
import mods.betterfoliage.resource.model.crossModelsRaw
import mods.betterfoliage.resource.model.crossModelsTextured
import mods.betterfoliage.resource.model.crossModelsTinted
import mods.betterfoliage.texture.LeafParticleRegistry
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.LazyMapInvalidatable
import mods.betterfoliage.util.averageColor
import mods.betterfoliage.util.isSnow
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Logger

object StandardLeafDiscovery : ConfigurableModelReplacer() {
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.leafBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.leafModels.modelList

    override fun processModel(
        state: BlockState,
        location: ResourceLocation,
        textureMatch: List<ResourceLocation>,
        sprites: MutableSet<ResourceLocation>,
        replacements: MutableMap<ResourceLocation, ModelBakeKey>
    ): Boolean {
        val leafType = LeafParticleRegistry.typeMappings.getType(textureMatch[0]) ?: "default"
        val generated = GeneratedLeaf(textureMatch[0], leafType)
            .register(Client.asyncPack)
            .apply { sprites.add(this) }

        detailLogger.log(INFO, "     particle $leafType")
        replacements[location] = StandardLeafKey(generated, leafType)
        return true
    }
}

fun TextureAtlasSprite.logColorOverride(logger: Logger, threshold: Double) {
    val hsb = averageColor
    return if (hsb.saturation >= threshold) {
        logger.log(INFO, "         brightness ${hsb.brightness}")
        logger.log(INFO, "         saturation ${hsb.saturation} >= ${threshold}, will use texture color")
    } else {
        logger.log(INFO, "         saturation ${hsb.saturation} < ${threshold}, will use block color")
    }
}

fun TextureAtlasSprite.getColorOverride(threshold: Double) = averageColor.let {
    if (it.saturation < threshold) null else it.copy(brightness = (it.brightness * 2.0f).coerceAtMost(0.9f))
}?.asColor?.let { Color(it) }

data class StandardLeafKey(
    val roundLeafTexture: ResourceLocation,
    val leafType: String
) : HalfBakedWrapKey() {
    override fun replace(wrapped: ISpecialRenderModel): ISpecialRenderModel {
        Atlas.BLOCKS[roundLeafTexture].logColorOverride(BetterFoliageMod.detailLogger(this), 0.1)
        return StandardLeafModel(wrapped, this)
    }
}

class StandardLeafModel(
    model: ISpecialRenderModel,
    key: StandardLeafKey
) : HalfBakedSpecialWrapper(model) {
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
            crossModelsTinted(leafModelsBase[key], Config.shortGrass.saturationThreshold) { key.roundLeafTexture }
        }
        val leafModelsSnowed = LazyMapInvalidatable(BakeWrapperManager) { key: StandardLeafKey ->
            crossModelsTextured(leafModelsBase[key], Color.white, false) { leafSpritesSnowed[it].name }
        }
    }
}