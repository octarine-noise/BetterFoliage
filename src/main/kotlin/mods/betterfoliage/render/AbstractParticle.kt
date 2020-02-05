package mods.betterfoliage.render

import mods.betterfoliage.util.Double3
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.texture.Sprite
import net.minecraft.world.World
import kotlin.math.cos
import kotlin.math.sin


abstract class AbstractParticle(world: World, x: Double, y: Double, z: Double) : SpriteBillboardParticle(world, x, y, z) {

    companion object {
//        @JvmStatic val sin = Array(64) { idx -> Math.sin(PI2 / 64.0 * idx) }
//        @JvmStatic val cos = Array(64) { idx -> Math.cos(PI2 / 64.0 * idx) }
    }

    val billboardRot = Pair(Double3.zero, Double3.zero)
    val currentPos = Double3.zero
    val prevPos = Double3.zero
    val velocity = Double3.zero

    override fun tick() {
        super.tick()
        currentPos.setTo(x, y, z)
        prevPos.setTo(prevPosX, prevPosY, prevPosZ)
        velocity.setTo(velocityX, velocityY, velocityZ)
        update()
        x = currentPos.x; y = currentPos.y; z = currentPos.z;
        velocityX = velocity.x; velocityY = velocity.y; velocityZ = velocity.z;
    }

    /** Render the particle. */
    abstract fun render(worldRenderer: BufferBuilder, partialTickTime: Float)

    /** Update particle on world tick. */
    abstract fun update()

    /** True if the particle is renderable. */
    abstract val isValid: Boolean

    /** Add the particle to the effect renderer if it is valid. */
    fun addIfValid() { if (isValid) MinecraftClient.getInstance().particleManager.addParticle(this) }

    override fun buildGeometry(buffer: BufferBuilder, camera: Camera, tickDelta: Float, rotX: Float, rotZ: Float, rotYZ: Float, rotXY: Float, rotXZ: Float) {
        billboardRot.first.setTo(rotX + rotXY, rotZ, rotYZ + rotXZ)
        billboardRot.second.setTo(rotX - rotXY, -rotZ, rotYZ - rotXZ)
        render(buffer, tickDelta)
    }

    /**
     * Render a particle quad.
     *
     * @param[tessellator] the [Tessellator] instance to use
     * @param[partialTickTime] partial tick time
     * @param[currentPos] render position
     * @param[prevPos] previous tick position for interpolation
     * @param[size] particle size
     * @param[rotation] viewpoint-dependent particle rotation (64 steps)
     * @param[sprite] particle texture
     * @param[isMirrored] mirror particle texture along V-axis
     * @param[alpha] aplha blending
     */
    fun renderParticleQuad(worldRenderer: BufferBuilder,
                           partialTickTime: Float,
                           currentPos: Double3 = this.currentPos,
                           prevPos: Double3 = this.prevPos,
                           size: Double = scale.toDouble(),
                           rotation: Double = 0.0,
                           sprite: Sprite = this.sprite,
                           isMirrored: Boolean = false,
                           alpha: Float = this.colorAlpha) {

        val minU = (if (isMirrored) sprite.minU else sprite.maxU).toDouble()
        val maxU = (if (isMirrored) sprite.maxU else sprite.minU).toDouble()
        val minV = sprite.minV.toDouble()
        val maxV = sprite.maxV.toDouble()

        val center = currentPos.copy().sub(prevPos).mul(partialTickTime.toDouble()).add(prevPos).sub(cameraX, cameraY, cameraZ)

        val cosRotation = cos(rotation); val sinRotation = sin(rotation)
        val v1 = Double3.weight(billboardRot.first, cosRotation * size, billboardRot.second, sinRotation * size)
        val v2 = Double3.weight(billboardRot.first, -sinRotation * size, billboardRot.second, cosRotation * size)

        val renderBrightness = this.getColorMultiplier(partialTickTime)
        val brHigh = renderBrightness shr 16 and 65535
        val brLow = renderBrightness and 65535

        worldRenderer
            .vertex(center.x - v1.x, center.y - v1.y, center.z - v1.z)
            .texture(maxU, maxV)
            .color(colorRed, colorGreen, colorBlue, alpha)
            .texture(brHigh, brLow)
            .next()

        worldRenderer
            .vertex(center.x - v2.x, center.y - v2.y, center.z - v2.z)
            .texture(maxU, minV)
            .color(colorRed, colorGreen, colorBlue, alpha)
            .texture(brHigh, brLow)
            .next()

        worldRenderer
            .vertex(center.x + v1.x, center.y + v1.y, center.z + v1.z)
            .texture(minU, minV)
            .color(colorRed, colorGreen, colorBlue, alpha)
            .texture(brHigh, brLow)
            .next()

        worldRenderer
            .vertex(center.x + v2.x, center.y + v2.y, center.z + v2.z)
            .texture(minU, maxV)
            .color(colorRed, colorGreen, colorBlue, alpha)
            .texture(brHigh, brLow)
            .next()
    }

    override fun getType() = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    fun setColor(color: Int) {
        colorBlue = (color and 255) / 256.0f
        colorGreen = ((color shr 8) and 255) / 256.0f
        colorRed = ((color shr 16) and 255) / 256.0f
    }
}

