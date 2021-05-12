package mods.betterfoliage.resource.generated

import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.blendRGB
import mods.betterfoliage.util.bytes
import mods.betterfoliage.util.get
import mods.betterfoliage.util.loadSprite
import mods.betterfoliage.util.set
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage

/**
 * Generate Short Grass textures from [Blocks.tallgrass] block textures.
 * The bottom 3/8 of the base texture is chopped off.
 *
 * @param[domain] Resource domain of generator
 */
data class GeneratedGrass(val baseSprite: ResourceLocation, val isSnowed: Boolean, val atlas: Atlas = Atlas.BLOCKS) {
    constructor(sprite: String, isSnowed: Boolean) : this(ResourceLocation(sprite), isSnowed)

    fun register(pack: GeneratedTexturePack) = pack.register(atlas, this, this::draw)

    fun draw(resourceManager: IResourceManager): ByteArray {
        val baseTexture = resourceManager.loadSprite(atlas.file(baseSprite))

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
            for (x in 0 until result.width) for (y in 0 until result.height) {
                result[x, y] = blendRGB(result[x, y], 16777215, 2, 3)
            }
        }
        return result.bytes
    }
}