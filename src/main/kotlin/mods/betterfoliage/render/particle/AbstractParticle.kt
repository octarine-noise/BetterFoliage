package mods.betterfoliage.render.particle

import mods.betterfoliage.util.Double3
import net.minecraft.client.MinecraftClient
import net.minecraft.client.particle.SpriteBillboardParticle
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Vec3f
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

abstract class AbstractParticle(world: ClientWorld, x: Double, y: Double, z: Double) : SpriteBillboardParticle(world, x, y, z) {

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

    /** Update particle on world tick. */
    abstract fun update()

    /** True if the particle is renderable. */
    abstract val isValid: Boolean

    /** Add the particle to the effect renderer if it is valid. */
    fun addIfValid() { if (isValid) MinecraftClient.getInstance().particleManager.addParticle(this) }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        renderParticleQuad(vertexConsumer, camera, tickDelta)
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
    fun renderParticleQuad(vertexConsumer: VertexConsumer,
                           camera: Camera,
                           tickDelta: Float,
                           currentPos: Double3 = this.currentPos,
                           prevPos: Double3 = this.prevPos,
                           size: Double = scale.toDouble(),
                           currentAngle: Float = this.angle,
                           prevAngle: Float = this.prevAngle,
                           sprite: Sprite = this.sprite,
                           alpha: Float = this.colorAlpha) {

        val center = Double3.lerp(tickDelta.toDouble(), prevPos, currentPos)
        val angle = MathHelper.lerp(tickDelta, prevAngle, currentAngle)
        val rotation = camera.rotation.copy().apply { hamiltonProduct(Vec3f.POSITIVE_Z.getRadialQuaternion(angle)) }
        val lightmapCoord = getBrightness(tickDelta)

        val coords = arrayOf(
            Double3(-1.0, -1.0, 0.0),
            Double3(-1.0, 1.0, 0.0),
            Double3(1.0, 1.0, 0.0),
            Double3(1.0, -1.0, 0.0)
        ).map { it.rotate(rotation).mul(size).add(center).sub(camera.pos.x, camera.pos.y, camera.pos.z) }

        fun renderVertex(vertex: Double3, u: Float, v: Float) = vertexConsumer
            .vertex(vertex.x, vertex.y, vertex.z).texture(u, v)
            .color(colorRed, colorGreen, colorBlue, alpha).light(lightmapCoord)
            .next()

        renderVertex(coords[0], sprite.maxU, sprite.maxV)
        renderVertex(coords[1], sprite.maxU, sprite.minV)
        renderVertex(coords[2], sprite.minU, sprite.minV)
        renderVertex(coords[3], sprite.minU, sprite.maxV)
    }

    fun setColor(color: Int) {
        colorBlue = (color and 255) / 256.0f
        colorGreen = ((color shr 8) and 255) / 256.0f
        colorRed = ((color shr 16) and 255) / 256.0f
    }
}

