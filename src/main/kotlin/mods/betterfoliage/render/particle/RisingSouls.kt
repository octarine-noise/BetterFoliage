package mods.betterfoliage.render.particle

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.model.SpriteDelegate
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.util.*
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class RisingSoulParticle(
    world: World, pos: BlockPos
) : AbstractParticle(
    world, pos.x.toDouble() + 0.5, pos.y.toDouble() + 1.0, pos.z.toDouble() + 0.5
) {

    val particleTrail: Deque<Double3> = LinkedList()
    val initialPhase = randomD(max = PI2)

    init {
        velocityY = 0.1
        gravityStrength = 0.0f
        sprite = headIcons[randomI(max = 1024)]
        maxAge = MathHelper.floor((0.6 + 0.4 * randomD()) * BetterFoliage.config.risingSoul.lifetime * 20.0)
    }

    override val isValid: Boolean get() = true

    override fun update() {
        val phase = initialPhase + (age.toDouble() * PI2 / 64.0)
        val cosPhase = cos(phase);
        val sinPhase = sin(phase)
        velocity.setTo(BetterFoliage.config.risingSoul.perturb.let { Double3(cosPhase * it, 0.1, sinPhase * it) })

        particleTrail.addFirst(currentPos.copy())
        while (particleTrail.size > BetterFoliage.config.risingSoul.trailLength) particleTrail.removeLast()

        if (!BetterFoliage.config.enabled) markDead()
    }

    override fun buildGeometry(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        var alpha = BetterFoliage.config.risingSoul.opacity.toFloat()
        if (age > maxAge - 40) alpha *= (maxAge - age) / 40.0f

        renderParticleQuad(
            vertexConsumer, camera, tickDelta,
            size = BetterFoliage.config.risingSoul.headSize * 0.25,
            alpha = alpha
        )

        var scale = BetterFoliage.config.risingSoul.trailSize * 0.25
        particleTrail.forEachPairIndexed { idx, current, previous ->
            scale *= BetterFoliage.config.risingSoul.sizeDecay
            alpha *= BetterFoliage.config.risingSoul.opacityDecay.toFloat()
            if (idx % BetterFoliage.config.risingSoul.trailDensity == 0)
                renderParticleQuad(
                    vertexConsumer, camera, tickDelta,
                    currentPos = current,
                    prevPos = previous,
                    size = scale,
                    alpha = alpha,
                    sprite = trackIcon
                )
        }
    }

    override fun getType() = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT

    companion object {
        val headIcons by SpriteSetDelegate(Atlas.PARTICLES) { idx ->
            Identifier(BetterFoliage.MOD_ID, "particle/rising_soul_$idx")
        }
        val trackIcon by SpriteDelegate(Atlas.PARTICLES) { Identifier(BetterFoliage.MOD_ID, "particle/soul_track") }
    }
}
