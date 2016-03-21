package mods.octarinecore.client.render

import mods.octarinecore.PI2
import mods.octarinecore.common.Double3
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.entity.Entity
import net.minecraft.world.World

abstract class AbstractEntityFX(world: World, x: Double, y: Double, z: Double) : EntityFX(world, x, y, z) {

    companion object {
        @JvmStatic val sin = Array(64) { idx -> Math.sin(PI2 / 64.0 * idx) }
        @JvmStatic val cos = Array(64) { idx -> Math.cos(PI2 / 64.0 * idx) }
    }

    val billboardRot = Pair(Double3.zero, Double3.zero)
    val currentPos = Double3.zero
    val prevPos = Double3.zero
    val velocity = Double3.zero

    override fun onUpdate() {
        super.onUpdate()
        currentPos.setTo(posX, posY, posZ)
        prevPos.setTo(prevPosX, prevPosY, prevPosZ)
        velocity.setTo(xSpeed, ySpeed, zSpeed)
        update()
        posX = currentPos.x; posY = currentPos.y; posZ = currentPos.z;
        xSpeed = velocity.x; ySpeed = velocity.y; zSpeed = velocity.z;
    }

    /** Render the particle. */
    abstract fun render(worldRenderer: VertexBuffer, partialTickTime: Float)

    /** Update particle on world tick. */
    abstract fun update()

    /** True if the particle is renderable. */
    abstract val isValid: Boolean

    /** Add the particle to the effect renderer if it is valid. */
    fun addIfValid() { if (isValid) Minecraft.getMinecraft().effectRenderer.addEffect(this) }

    override fun renderParticle(worldRenderer: VertexBuffer, entity: Entity, partialTickTime: Float, rotX: Float, rotZ: Float, rotYZ: Float, rotXY: Float, rotXZ: Float) {
        billboardRot.first.setTo(rotX + rotXY, rotZ, rotYZ + rotXZ)
        billboardRot.second.setTo(rotX - rotXY, -rotZ, rotYZ - rotXZ)
        render(worldRenderer, partialTickTime)
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
     * @param[icon] particle texture
     * @param[isMirrored] mirror particle texture along V-axis
     * @param[alpha] aplha blending
     */
    fun renderParticleQuad(worldRenderer: VertexBuffer,
                           partialTickTime: Float,
                           currentPos: Double3 = this.currentPos,
                           prevPos: Double3 = this.prevPos,
                           size: Double = particleScale.toDouble(),
                           rotation: Int = 0,
                           icon: TextureAtlasSprite = particleTexture,
                           isMirrored: Boolean = false,
                           alpha: Float = this.particleAlpha) {

        val minU = (if (isMirrored) icon.minU else icon.maxU).toDouble()
        val maxU = (if (isMirrored) icon.maxU else icon.minU).toDouble()
        val minV = icon.minV.toDouble()
        val maxV = icon.maxV.toDouble()

        val center = currentPos.copy().sub(prevPos).mul(partialTickTime.toDouble()).add(prevPos).sub(interpPosX, interpPosY, interpPosZ)
        val v1 = if (rotation == 0) billboardRot.first * size else
            Double3.weight(billboardRot.first, cos[rotation and 63] * size, billboardRot.second, sin[rotation and 63] * size)
        val v2 = if (rotation == 0) billboardRot.second * size else
            Double3.weight(billboardRot.first, -sin[rotation and 63] * size, billboardRot.second, cos[rotation and 63] * size)

        val renderBrightness = this.getBrightnessForRender(partialTickTime)
        val brLow = renderBrightness shr 16 and 65535
        val brHigh = renderBrightness and 65535

        worldRenderer
            .pos(center.x - v1.x, center.y - v1.y, center.z - v1.z)
            .tex(maxU, maxV)
            .color(particleRed, particleGreen, particleBlue, alpha)
            .lightmap(brLow, brHigh)
            .endVertex()

        worldRenderer
            .pos(center.x - v2.x, center.y - v2.y, center.z - v2.z)
            .tex(maxU, minV)
            .color(particleRed, particleGreen, particleBlue, alpha)
            .lightmap(brLow, brHigh)
            .endVertex()

        worldRenderer
            .pos(center.x + v1.x, center.y + v1.y, center.z + v1.z)
            .tex(minU, minV)
            .color(particleRed, particleGreen, particleBlue, alpha)
            .lightmap(brLow, brHigh)
            .endVertex()

        worldRenderer
            .pos(center.x + v2.x, center.y + v2.y, center.z + v2.z)
            .tex(minU, maxV)
            .color(particleRed, particleGreen, particleBlue, alpha)
            .lightmap(brLow, brHigh)
            .endVertex()
    }

    override fun getFXLayer() = 1

    fun setColor(color: Int) {
        particleBlue = (color and 255) / 256.0f
        particleGreen = ((color shr 8) and 255) / 256.0f
        particleRed = ((color shr 16) and 255) / 256.0f
    }
}