package mods.betterfoliage.resource.generated

import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.bytes
import mods.betterfoliage.util.loadSprite
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage
import java.lang.Math.max

data class CenteredSprite(val sprite: Identifier, val atlas: Atlas = Atlas.BLOCKS, val aspectHeight: Int = 1, val aspectWidth: Int = 1) {

    fun register(pack: GeneratedBlockTexturePack) = pack.register(this, this::draw)

    fun draw(resourceManager: ResourceManager): ByteArray {
        val baseTexture = resourceManager.loadSprite(atlas.wrap(sprite))

        val frameWidth = baseTexture.width
        val frameHeight = baseTexture.width * aspectHeight / aspectWidth
        val frames = baseTexture.height / frameHeight
        val size = max(frameWidth, frameHeight)

        val result = BufferedImage(size, size * frames, BufferedImage.TYPE_4BYTE_ABGR)
        val graphics = result.createGraphics()

        // iterate all frames
        for (frame in 0 until frames) {
            val baseFrame = baseTexture.getSubimage(0, size * frame, frameWidth, frameHeight)
            val resultFrame = BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR)

            resultFrame.createGraphics().apply {
                drawImage(baseFrame, (size - frameWidth) / 2, (size - frameHeight) / 2, null)
            }
            graphics.drawImage(resultFrame, 0, size * frame, null)
        }

        return result.bytes
    }
}