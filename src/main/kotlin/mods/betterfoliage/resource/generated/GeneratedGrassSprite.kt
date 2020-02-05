package mods.betterfoliage.resource.generated

import mods.betterfoliage.util.*
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage

/**
 * Generate Short Grass textures from [Blocks.tallgrass] block textures.
 * The bottom 3/8 of the base texture is chopped off.
 *
 * @param[domain] Resource domain of generator
 */
data class GeneratedGrassSprite(val sprite: Identifier, val isSnowed: Boolean, val atlas: Atlas = Atlas.BLOCKS) {
    constructor(sprite: String, isSnowed: Boolean) : this(Identifier(sprite), isSnowed)

    fun register(pack: GeneratedBlockTexturePack) = pack.register(this, this::draw)

    fun draw(resourceManager: ResourceManager): ByteArray {
        val baseTexture = resourceManager.loadSprite(atlas.wrap(sprite))

        val result = BufferedImage(baseTexture.width, baseTexture.height, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = result.createGraphics()

        val size = baseTexture.width
        val frames = baseTexture.height / size

        // iterate all frames
        for (frame in 0 until frames) {
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
        if (isSnowed) {
            for (x in 0..result.width - 1) for (y in 0..result.height - 1) {
                result[x, y] = blendRGB(result[x, y], 16777215, 2, 3)
            }
        }
        return result.bytes
    }
}