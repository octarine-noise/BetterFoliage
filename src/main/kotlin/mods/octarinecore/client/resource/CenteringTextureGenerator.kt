package mods.octarinecore.client.resource

import java.awt.image.BufferedImage
import java.lang.Math.*

class CenteringTextureGenerator(domain: String, val aspectWidth: Int, val aspectHeight: Int) : TextureGenerator(domain) {

    override fun generate(params: ParameterList): BufferedImage? {
        val target = targetResource(params)!!
        val baseTexture = resourceManager[target.second]?.loadImage() ?: return null

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

        return resultTexture
    }
}