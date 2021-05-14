package mods.betterfoliage.render.particle

import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.model.SpriteDelegate
import mods.betterfoliage.model.SpriteSetDelegate
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.forEachPairIndexed
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomI
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.Deque
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.sin

class RisingSoulParticle(
    world: ClientWorld, pos: BlockPos
) : AbstractParticle(
    world, pos.x.toDouble() + 0.5, pos.y.toDouble() + 1.0, pos.z.toDouble() + 0.5
) {

    val particleTrail: Deque<Double3> = LinkedList<Double3>()
    val initialPhase = randomD(max = PI2)

    init {
        yd = 0.1
        gravity = 0.0f
        sprite = headIcons[randomI(max = 1024)]
        lifetime = MathHelper.floor((0.6 + 0.4 * randomD()) * Config.risingSoul.lifetime * 20.0)
    }

    override val isValid: Boolean get() = true

    override fun update() {
        val phase = initialPhase + (age.toDouble() * PI2 / 64.0)
        val cosPhase = cos(phase);
        val sinPhase = sin(phase)
        velocity.setTo(Config.risingSoul.perturb.let { Double3(cosPhase * it, 0.1, sinPhase * it) })

        particleTrail.addFirst(currentPos.copy())
        while (particleTrail.size > Config.risingSoul.trailLength) particleTrail.removeLast()

        if (!Config.enabled) remove()
    }

    override fun render(vertexBuilder: IVertexBuilder, camera: ActiveRenderInfo, tickDelta: Float) {
        var alpha = Config.risingSoul.opacity.toFloat()
        if (age > lifetime - 40) alpha *= (lifetime - age) / 40.0f

        renderParticleQuad(
            vertexBuilder, camera, tickDelta,
            size = Config.risingSoul.headSize * 0.25,
            alpha = alpha
        )

        var scale = Config.risingSoul.trailSize * 0.25
        particleTrail.forEachPairIndexed { idx, current, previous ->
            scale *= Config.risingSoul.sizeDecay
            alpha *= Config.risingSoul.opacityDecay.toFloat()
            if (idx % Config.risingSoul.trailDensity == 0)
                renderParticleQuad(
                    vertexBuilder, camera, tickDelta,
                    currentPos = current,
                    prevPos = previous,
                    size = scale,
                    alpha = alpha,
                    sprite = trackIcon
                )
        }
    }

    override fun getRenderType(): IParticleRenderType = IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

    companion object {
        val headIcons by SpriteSetDelegate(Atlas.PARTICLES) { idx ->
            ResourceLocation(BetterFoliageMod.MOD_ID, "particle/rising_soul_$idx")
        }
        val trackIcon by SpriteDelegate(Atlas.PARTICLES) { ResourceLocation(BetterFoliageMod.MOD_ID, "particle/soul_track") }
    }
}
