package mods.betterfoliage.render.particle

import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.util.Double3
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.SpriteTexturedParticle
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3f

abstract class AbstractParticle(world: ClientWorld, x: Double, y: Double, z: Double) : SpriteTexturedParticle(world, x, y, z) {

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
        prevPos.setTo(xo, yo, zo)
        velocity.setTo(xd, yd, zd)
        update()
        x = currentPos.x; y = currentPos.y; z = currentPos.z;
        xd = velocity.x; yd = velocity.y; zd = velocity.z;
    }

    /** Update particle on world tick. */
    abstract fun update()

    /** True if the particle is renderable. */
    abstract val isValid: Boolean

    /** Add the particle to the effect renderer if it is valid. */
    fun addIfValid() { if (isValid) Minecraft.getInstance().particleEngine.add(this) }

    override fun render(vertexBuilder: IVertexBuilder, camera: ActiveRenderInfo, tickDelta: Float) {
        super.render(vertexBuilder, camera, tickDelta)
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
                           size: Double = quadSize.toDouble(),
                           currentAngle: Float = this.roll,
                           prevAngle: Float = this.oRoll,
                           sprite: TextureAtlasSprite = this.sprite,
                           alpha: Float = this.alpha) {

        val center = Double3.lerp(tickDelta.toDouble(), prevPos, currentPos)
        val angle = MathHelper.lerp(tickDelta, prevAngle, currentAngle)
        val rotation = camera.rotation().copy().apply { mul(Vector3f.ZP.rotation(angle)) }
        val lightmapCoord = getLightColor(tickDelta)

        val coords = arrayOf(
            Double3(-1.0, -1.0, 0.0),
            Double3(-1.0, 1.0, 0.0),
            Double3(1.0, 1.0, 0.0),
            Double3(1.0, -1.0, 0.0)
        ).map { it.rotate(rotation).mul(size).add(center).sub(camera.position.x, camera.position.y, camera.position.z) }

        fun renderVertex(vertex: Double3, u: Float, v: Float) = vertexConsumer
            .vertex(vertex.x, vertex.y, vertex.z).uv(u, v)
            .color(rCol, gCol, bCol, alpha).uv2(lightmapCoord)
            .endVertex()

        renderVertex(coords[0], sprite.u1, sprite.v1)
        renderVertex(coords[1], sprite.u1, sprite.v0)
        renderVertex(coords[2], sprite.u0, sprite.v0)
        renderVertex(coords[3], sprite.u0, sprite.v1)
    }

    fun setColor(color: Int) {
        bCol = (color and 255) / 256.0f
        gCol = ((color shr 8) and 255) / 256.0f
        rCol = ((color shr 16) and 255) / 256.0f
    }
}

