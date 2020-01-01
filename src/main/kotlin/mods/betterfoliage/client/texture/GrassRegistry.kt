package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.HSB
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import java.lang.Math.min

const val defaultGrassColor = 0

/** Rendering-related information for a grass block. */
class GrassInfo(
    /** Top texture of the grass block. */
    val grassTopTexture: TextureAtlasSprite,

    /**
     * Color to use for Short Grass rendering instead of the biome color.
     *
     * Value is null if the texture is mostly grey (the saturation of its average color is under a configurable limit),
     * the average color of the texture otherwise.
     */
    val overrideColor: Int?
)

object GrassRegistry : ModelRenderRegistryRoot<GrassInfo>()

object StandardGrassRegistry : ModelRenderRegistryConfigurable<GrassInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.grassBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.grassModels.modelList
    override fun processModel(state: BlockState, textures: List<String>) = StandardGrassKey(logger, textures[0])
    init { BetterFoliage.modBus.register(this) }
}

class StandardGrassKey(override val logger: Logger, val textureName: String) : ModelRenderKey<GrassInfo> {
    override fun resolveSprites(atlas: AtlasTexture): GrassInfo {
        val logName = "StandardGrassKey"
        val texture = atlas[textureName] ?: missingSprite
        logger.log(Level.DEBUG, "$logName: texture $textureName")
        val hsb = HSB.fromColor(texture.averageColor ?: defaultGrassColor)
        val overrideColor = if (hsb.saturation >= Config.shortGrass.saturationThreshold) {
            logger.log(Level.DEBUG, "$logName:         brightness ${hsb.brightness}")
            logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} >= ${Config.shortGrass.saturationThreshold}, using texture color")
            hsb.copy(brightness = min(0.9f, hsb.brightness * 2.0f)).asColor
        } else {
            logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} < ${Config.shortGrass.saturationThreshold}, using block color")
            null
        }
        return GrassInfo(texture, overrideColor)
    }
}