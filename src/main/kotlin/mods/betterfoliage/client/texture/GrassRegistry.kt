package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.resource.Identifier
import mods.octarinecore.client.render.lighting.HSB
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import org.apache.logging.log4j.Level
import java.lang.Math.min
import java.util.concurrent.CompletableFuture

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

object AsyncGrassDiscovery : ConfigurableModelDiscovery<GrassInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses: ConfigurableBlockMatcher get() = BlockConfig.grassBlocks
    override val modelTextures: List<ModelTextureList> get() = BlockConfig.grassModels.modelList

    override fun processModel(state: BlockState, textures: List<String>, atlas: AtlasFuture): CompletableFuture<GrassInfo> {
        val textureName = textures[0]
        val spriteF = atlas.sprite(Identifier(textureName))
        logger.log(Level.DEBUG, "$logName:       texture $textureName")
        return atlas.mapAfter {
            val sprite = spriteF.get()
            logger.log(Level.DEBUG, "$logName: block state $state")
            logger.log(Level.DEBUG, "$logName:       texture $textureName")
            val hsb = HSB.fromColor(sprite.averageColor)
            val overrideColor = if (hsb.saturation >= Config.shortGrass.saturationThreshold) {
                logger.log(Level.DEBUG, "$logName:         brightness ${hsb.brightness}")
                logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} >= ${Config.shortGrass.saturationThreshold}, using texture color")
                hsb.copy(brightness = min(0.9f, hsb.brightness * 2.0f)).asColor
            } else {
                logger.log(Level.DEBUG, "$logName:         saturation ${hsb.saturation} < ${Config.shortGrass.saturationThreshold}, using block color")
                null
            }
            GrassInfo(sprite, overrideColor)
        }
    }

    fun init() {
        GrassRegistry.registries.add(this)
        BetterFoliage.blockSprites.providers.add(this)
    }
}