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

        val result = BufferedImage(baseTexture.width, baseTexture.height, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = result.createGraphics()

        val size = baseTexture.width
        val frames = baseTexture.height / size

        // iterate all frames
        for (frame in 0 .. frames - 1) {
            val baseFrame = baseTexture.getSubimage(0, size * frame, size, size)
            val grassFrame = BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR)

            // draw bottom half of texture
            grassFrame.createGraphics().apply {
                drawImage(baseFrame, 0, 3 * size / 8, null)
            }

            // add to animated png
            graphics.drawImage(grassFrame, 0, size * frame, null)
        }

        // blend with white if snowed
        if (isSnowed && target.first == ResourceType.COLOR) {
            for (x in 0..result.width - 1) for (y in 0..result.height - 1) {
                result[x, y] = blendRGB(result[x, y], 16777215, 2, 3)
            }
        }

        return result
    }
}
