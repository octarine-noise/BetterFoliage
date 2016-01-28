package mods.betterfoliage.client.texture

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.HSB
import mods.octarinecore.client.resource.BlockTextureInspector
import mods.octarinecore.client.resource.averageColor
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

const val defaultGrassColor = 0

/** Rendering-related information for a grass block. */
class GrassInfo(
    /** Top texture of the grass block. */
    val grassTopTexture: TextureAtlasSprite,

    /**
     * Color to use for Short Grass rendering instead of the biome color.
     *
     * Value is null if the texture is mostly grey (the saturation of its average color is under a configurable limit),
     * the average color of the texture (significantly brightened) otherwise.
     */
    val overrideColor: Int?
)

/** Collects and manages rendering-related information for grass blocks. */
@SideOnly(Side.CLIENT)
object GrassRegistry : BlockTextureInspector<GrassInfo>() {

    init {
        matchClassAndModel(Config.blocks.grass, "block/grass", listOf("top"))
        matchClassAndModel(Config.blocks.grass, "block/cube_bottom_top", listOf("top"))
    }

    override fun onAfterModelLoad() {
        super.onAfterModelLoad()
        Client.log(INFO, "Inspecting grass textures")
    }

    override fun processTextures(state: IBlockState, textures: List<TextureAtlasSprite>, atlas: TextureMap): GrassInfo {
        val hsb = HSB.fromColor(textures[0].averageColor ?: defaultGrassColor)
        val overrideColor = if (hsb.saturation > Config.shortGrass.saturationThreshold) hsb.copy(brightness = 0.8f).asColor else null
        return GrassInfo(textures[0], overrideColor)
    }
}