package mods.betterfoliage.client.texture

import mods.betterfoliage.BetterFoliage
import mods.octarinecore.client.resource.*
import mods.octarinecore.stripStart
import net.minecraft.resources.IResource
import net.minecraft.util.ResourceLocation
import net.minecraftforge.resource.VanillaResourceType.TEXTURES
import java.awt.image.BufferedImage
import java.io.InputStream

/**
 * Generate round leaf textures from leaf block textures.
 * The base texture is tiled 2x2, then parts of it are made transparent by applying a mask to the alpha channel.
 *
 * Generator parameter _type_: Leaf type (configurable by user). Different leaf types may have their own alpha mask.
 *
 * @param[domain] Resource domain of generator
 */
class LeafGenerator(domain: String) : GeneratorBase<LeafGenerator.Key>(domain, TEXTURES) {

    override val locationMapper = Atlas.BLOCKS::unwrap

    fun register(texture: ResourceLocation, leafType: String) = registerResource(Key(texture, leafType))

    override fun exists(key: Key) = resourceManager.hasResource(Atlas.BLOCKS.wrap(key.texture))

    override fun get(key: Key): InputStream? {

//        val handDrawnLoc = Atlas.BLOCKS.wrap(key.texture)
//        resourceManager[handDrawnLoc]?.loadImage()?.let { return it.asStream }

        val baseTexture = resourceManager[Atlas.BLOCKS.wrap(key.texture)]?.loadImage() ?: return null
        val size = baseTexture.width
        val frames = baseTexture.height / size

        val maskTexture = (getLeafMask(key.leafType, size * 2) ?: getLeafMask("default", size * 2))?.loadImage()
        fun scale(i: Int) = i * maskTexture!!.width / (size * 2)

        val leafTexture = BufferedImage(size * 2, size * 2 * frames, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = leafTexture.createGraphics()

        // iterate all frames
        for (frame in 0 .. frames - 1) {
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
                for (x in 0 .. size * 2 - 1) for (y in 0 .. size * 2 - 1) {
                    val basePixel = leafFrame[x, y].toLong() and 0xFFFFFFFFL
                    val maskPixel = maskTexture[scale(x), scale(y)].toLong() and 0xFF000000L or 0xFFFFFFL
                    leafFrame[x, y] = (basePixel and maskPixel).toInt()
                }
            }

            // add to animated png
            graphics.drawImage(leafFrame, 0, size * frame * 2, null)
        }

        return leafTexture.asStream
    }

    /**
     * Get the alpha mask to use
     *
     * @param[type] Alpha mask type.
     * @param[maxSize] Preferred mask size.
     */
    fun getLeafMask(type: String, maxSize: Int) = getMultisizeTexture(maxSize) { size ->
        ResourceLocation(BetterFoliage.MOD_ID, "textures/blocks/leafmask_${size}_${type}.png")
    }

    /**
     * Get a texture resource when multiple sizes may exist.
     *
     * @param[maxSize] Maximum size to consider. This value is progressively halved when searching for smaller versions.
     * @param[maskPath] Location of the texture of the given size
     *
     */
    fun getMultisizeTexture(maxSize: Int, maskPath: (Int)->ResourceLocation): IResource? {
        var size = maxSize
        val sizes = mutableListOf<Int>()
        while(size > 2) { sizes.add(size); size /= 2 }
        return sizes.map { resourceManager[maskPath(it)] }.filterNotNull().firstOrNull()
    }

    data class Key(val texture: ResourceLocation, val leafType: String)
}