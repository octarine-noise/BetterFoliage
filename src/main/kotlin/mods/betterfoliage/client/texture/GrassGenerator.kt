package mods.betterfoliage.client.texture

import mods.betterfoliage.client.Client
import mods.octarinecore.client.resource.*
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level
import java.awt.image.BufferedImage

/**
 * Generate Short Grass textures from [Blocks.tallgrass] block textures.
 * The bottom 3/8 of the base texture is chopped off.
 *
 * @param[domain] Resource domain of generator
 */
class GrassGenerator(domain: String) : TextureGenerator(domain) {

    override fun generate(params: ParameterList): BufferedImage? {
        val target = targetResource(params)!!
        val isSnowed = params["snowed"]?.toBoolean() ?: false

        val baseTexture = resourceManager[target.second]?.loadImage() ?: return null

        // draw bottom half of texture
        val result = BufferedImage(baseTexture.width, baseTexture.height, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = result.createGraphics()
        graphics.drawImage(baseTexture, 0, 3 * baseTexture.height / 8, null)

        // blend with white if snowed
        if (isSnowed && target.first == ResourceType.COLOR) {
            for (x in 0..result.width - 1) for (y in 0..result.height - 1) {
                result[x, y] = blendRGB(result[x, y], 16777215, 2, 3)
            }
        }

        return result
    }
}
