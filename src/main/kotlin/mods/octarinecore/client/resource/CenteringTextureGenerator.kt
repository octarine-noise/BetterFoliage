package mods.octarinecore.client.resource

import net.minecraft.util.ResourceLocation
import net.minecraftforge.resource.VanillaResourceType
import java.awt.image.BufferedImage
import java.io.InputStream
import java.lang.Math.*

class CenteringTextureGenerator(
    domain: String,
    val aspectWidth: Int,
    val aspectHeight: Int
) : GeneratorBase<ResourceLocation>(domain, VanillaResourceType.TEXTURES) {

    override val locationMapper = Atlas.BLOCKS::unwrap

    override fun exists(key: ResourceLocation) = resourceManager.hasResource(Atlas.BLOCKS.wrap(key))

    override fun get(key: ResourceLocation): InputStream? {
        val baseTexture = resourceManager[Atlas.BLOCKS.wrap(key)]?.loadImage() ?: return null

        val frameWidth = baseTexture.width
        val frameHeight = baseTexture.width * aspectHeight / aspectWidth
        val frames = baseTexture.height / frameHeight
        val size = max(frameWidth, frameHeight)

        val resultTexture = BufferedImage(size, size * frames, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = resultTexture.createGraphics()

        // iterate all frames
        for (frame in 0 .. frames - 1) {
            val baseFrame = baseTexture.getSubimage(0, size * frame, frameWidth, frameHeight)
            val resultFrame = BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR)

            resultFrame.createGraphics().apply {
                drawImage(baseFrame, (size - frameWidth) / 2, (size - frameHeight) / 2, null)
            }
            graphics.drawImage(resultFrame, 0, size * frame, null)
        }

        return resultTexture.asStream
    }
}