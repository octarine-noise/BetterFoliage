package mods.betterfoliage.resource.generated

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.util.*
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage

/**
 * Generate round leaf textures from leaf block textures.
 * The base texture is tiled 2x2, then parts of it are made transparent by applying a mask to the alpha channel.
 *
 * Different leaf types may have their own alpha mask.
 *
 * @param[domain] Resource domain of generator
 */
data class GeneratedLeafSprite(val sprite: Identifier, val leafType: String, val atlas: Atlas = Atlas.BLOCKS) {

    fun register(pack: GeneratedBlockTexturePack) = pack.register(this, this::draw)

    fun draw(resourceManager: ResourceManager): ByteArray {
        val baseTexture = resourceManager.loadSprite(atlas.file(sprite))

        val size = baseTexture.width
        val frames = baseTexture.height / size

        val maskTexture = (getLeafMask(leafType, size * 2) ?: getLeafMask("default", size * 2))?.loadImage()
        fun scale(i: Int) = i * maskTexture!!.width / (size * 2)

        val result = BufferedImage(size * 2, size * 2 * frames, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = result.createGraphics()

        // iterate all frames
        for (frame in 0 until frames) {
            val baseFrame = baseTexture.getSubimage(0, size * frame, size, size)
            val leafFrame = BufferedImage(size * 2, size * 2, BufferedImage.TYPE_4BYTE_ABGR)

            // tile leaf texture 2x2
            leafFrame.createGraphics().apply {
                drawImage(baseFrame, 0, 0, null)
                drawImage(baseFrame, 0, size, null)
                drawImage(baseFrame, size, 0, null)
                drawImage(baseFrame, size, size, null)
            }

            // overlay alpha mask
            if (maskTexture != null) {
                for (x in 0 until size * 2) for (y in 0 until size * 2) {
                    val basePixel = leafFrame[x, y].toLong() and 0xFFFFFFFFL
                    val maskPixel = maskTexture[scale(x), scale(y)].toLong() and 0xFF000000L or 0xFFFFFFL
                    leafFrame[x, y] = (basePixel and maskPixel).toInt()
                }
            }

            // add to animated png
            graphics.drawImage(leafFrame, 0, size * frame * 2, null)
        }

        return result.bytes
    }

    /**
     * Get the alpha mask to use
     *
     * @param[type] Alpha mask type.
     * @param[maxSize] Preferred mask size.
     */
    fun getLeafMask(type: String, maxSize: Int) = getMultisizeTexture(maxSize) { size ->
        Atlas.BLOCKS.file(Identifier(BetterFoliage.MOD_ID, "blocks/leafmask_${size}_${type}"))
    }

    /**
     * Get a texture resource when multiple sizes may exist.
     *
     * @param[maxSize] Maximum size to consider. This value is progressively halved when searching for smaller versions.
     * @param[maskPath] Location of the texture of the given size
     *
     */
    fun getMultisizeTexture(maxSize: Int, maskPath: (Int)->Identifier): Resource? {
        var size = maxSize
        val sizes = mutableListOf<Int>()
        while(size > 2) { sizes.add(size); size /= 2 }
        return sizes.findFirst { resourceManager[maskPath(it)] }
    }
}