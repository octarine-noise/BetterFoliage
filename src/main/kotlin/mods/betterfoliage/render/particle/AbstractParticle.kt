package mods.betterfoliage.render.particle

import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.util.Double3
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.SpriteTexturedParticle
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.Vector3f
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

abstract class AbstractParticle(world: World, x: Double, y: Double, z: Double) : SpriteTexturedParticle(world, x, y, z) {

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
        currentPos.setTo(posX, posY, posZ)
        prevPos.setTo(prevPosX, prevPosY, prevPosZ)
        velocity.setTo(motionX, motionY, motionZ)
        update()
        posX = currentPos.x; posY = currentPos.y; posZ = currentPos.z;
        motionX = velocity.x; motionY = velocity.y; motionZ = velocity.z;
    }

    /** Update particle on world tick. */
    abstract fun update()

    /** True if the particle is renderable. */
    abstract val isValid: Boolean

    /** Add the particle to the effect renderer if it is valid. */
    fun addIfValid() { if (isValid) Minecraft.getInstance().particles.addEffect(this) }

    override fun renderParticle(vertexBuilder: IVertexBuilder, camera: ActiveRenderInfo, tickDelta: Float) {
        super.renderParticle(vertexBuilder, camera, tickDelta)
    }

    /**
     * Render a particle quad.
     *
     * @param[tessellator] the [Tessellator] instance to use
     * @param[tickDelta] partial tick time
     * @param[currentPos] render position
     * @param[prevPos] previous tick position for interpolation
     * @param[size] particle size
     * @param[currentAngle] viewpoint-dependent particle rotation (64 steps)
     * @param[sprite] particle texture
     * @param[isMirrored] mirror particle texture along V-axis
     * @param[alpha] aplha blending
     */
    fun renderParticleQuad(vertexConsumer: IVertexBuilder,
                           camera: ActiveRenderInfo,
                           tickDelta: Float,
                           currentPos: Double3 = this.currentPos,
                           prevPos: Double3 = this.prevPos,
                           size: Double = particleScale.toDouble(),
                           currentAngle: Float = this.particleAngle,
                           prevAngle: Float = this.prevParticleAngle,
                           sprite: TextureAtlasSprite = this.sprite,
                           alpha: Float = this.particleAlpha) {

        val center = Double3.lerp(tickDelta.toDouble(), prevPos, currentPos)
        val angle = MathHelper.lerp(tickDelta, prevAngle, currentAngle)
        val rotation = camera.rotation.copy().apply { multiply(Vector3f.ZP.rotation(angle)) }
        val lightmapCoord = getBrightnessForRender(tickDelta)

        val coords = arrayOf(
            Double3(-1.0, -1.0, 0.0),
            Double3(-1.0, 1.0, 0.0),
            Double3(1.0, 1.0, 0.0),
            Double3(1.0, -1.0, 0.0)
        ).map { it.rotate(rotation).mul(size).add(center).sub(camera.projectedView.x, camera.projectedView.y, camera.projectedView.z) }

        fun renderVertex(vertex: Double3, u: Float, v: Float) = vertexConsumer
            .pos(vertex.x, vertex.y, vertex.z).tex(u, v)
            .color(particleRed, particleGreen, particleBlue, alpha).lightmap(lightmapCoord)
            .endVertex()

        renderVertex(coords[0], sprite.maxU, sprite.maxV)
        renderVertex(coords[1], sprite.maxU, sprite.minV)
        renderVertex(coords[2], sprite.minU, sprite.minV)
        renderVertex(coords[3], sprite.minU, sprite.maxV)
    }

    fun setColor(color: Int) {
        particleBlue = (color and 255) / 256.0f
        particleGreen = ((color shr 8) and 255) / 256.0f
        particleRed = ((color shr 16) and 255) / 256.0f
    }
}

