package mods.betterfoliage.util

import mods.betterfoliage.model.Color
import mods.betterfoliage.model.HSB
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.resource.Resource
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.atan2

enum class Atlas(val resourceId: Identifier) {
    BLOCKS(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE),
    PARTICLES(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);

    /** Get the fully-qualified resource name for sprites belonging to this atlas */
    fun file(resource: Identifier) = Identifier(resource.namespace, "textures/${resource.path}.png")

    /** Reference to the atlas itself */
    private val atlas: SpriteAtlasTexture get() = MinecraftClient.getInstance().textureManager.getTexture(resourceId) as SpriteAtlasTexture

    /** Get a sprite from this atlas */
    operator fun get(location: Identifier) = atlas.getSprite(location)
}

operator fun SpriteAtlasTexture.get(res: Identifier): Sprite? = getSprite(res)
operator fun SpriteAtlasTexture.get(name: String): Sprite? = getSprite(Identifier(name))

fun ResourceManager.loadSprite(id: Identifier) = this.get(id)?.loadImage() ?: throw IOException("Cannot load resource $id")

fun Resource.loadImage(): BufferedImage? = ImageIO.read(this.inputStream)

/** Index operator to get the RGB value of a pixel. */
operator fun BufferedImage.get(x: Int, y: Int) = this.getRGB(x, y)
/** Index operator to set the RGB value of a pixel. */
operator fun BufferedImage.set(x: Int, y: Int, value: Int) = this.setRGB(x, y, value)

val BufferedImage.bytes: ByteArray get() =
    ByteArrayOutputStream().let { ImageIO.write(this, "PNG", it); it.toByteArray() }

/**
 * Calculate the average color of an image.
 *
 * Only non-transparent pixels are considered. Averages are taken in the HSB color space (note: Hue is a circular average),
 * and the result transformed back to the RGB color space.
 */
val Sprite_images = YarnHelper.requiredField<Array<NativeImage>>("net.minecraft.class_1058", "field_5262", "[Lnet/minecraft/class_1011;")

val Sprite.averageColor: HSB get() {
    var numOpaque = 0
    var sumHueX = 0.0
    var sumHueY = 0.0
    var sumSaturation = 0.0f
    var sumBrightness = 0.0f
    for (x in 0 until width)
        for (y in 0 until height) {
            val pixel = this[Sprite_images]!![0].getPixelColor(x, y)
            val alpha = (pixel shr 24) and 255
            val hsb = HSB.fromColor(pixel)
            if (alpha == 255) {
                numOpaque++
                sumHueX += Math.cos((hsb.hue.toDouble() - 0.5) * PI2)
                sumHueY += Math.sin((hsb.hue.toDouble() - 0.5) * PI2)
                sumSaturation += hsb.saturation
                sumBrightness += hsb.brightness
            }
        }

    // circular average - transform sum vector to polar angle
    val avgHue = (atan2(sumHueY, sumHueX) / PI2 + 0.5).toFloat()
    return HSB(avgHue, sumSaturation / numOpaque.toFloat(), sumBrightness / numOpaque.toFloat())
}

/** Weighted blend of 2 packed RGB colors */
fun blendRGB(rgb1: Int, rgb2: Int, weight1: Int, weight2: Int): Int {
    val r = (((rgb1 shr 16) and 255) * weight1 + ((rgb2 shr 16) and 255) * weight2) / (weight1 + weight2)
    val g = (((rgb1 shr 8) and 255) * weight1 + ((rgb2 shr 8) and 255) * weight2) / (weight1 + weight2)
    val b = ((rgb1 and 255) * weight1 + (rgb2 and 255) * weight2) / (weight1 + weight2)
    val a = (rgb1 shr 24) and 255
    val result = ((a shl 24) or (r shl 16) or (g shl 8) or b)
    return result
}

fun logColorOverride(logger: Logger, threshold: Double, hsb: HSB) {
    return if (hsb.saturation >= threshold) {
        logger.log(Level.INFO, "         brightness ${hsb.brightness}")
        logger.log(Level.INFO, "         saturation ${hsb.saturation} >= ${threshold}, will use texture color")
    } else {
        logger.log(Level.INFO, "         saturation ${hsb.saturation} < ${threshold}, will use block color")
    }
}

fun HSB.colorOverride(threshold: Double) =
    if (saturation < threshold) null else copy(brightness = (brightness * 2.0f).coerceAtMost(0.9f)).asColor.let { Color(it) }