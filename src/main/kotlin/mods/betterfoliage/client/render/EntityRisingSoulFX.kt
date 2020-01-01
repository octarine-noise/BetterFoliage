package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractEntityFX
import mods.octarinecore.client.resource.Atlas
import mods.octarinecore.client.resource.ResourceHandler
import mods.octarinecore.common.Double3
import mods.octarinecore.forEachPairIndexed
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class EntityRisingSoulFX(world: World, pos: BlockPos) :
AbstractEntityFX(world, pos.x.toDouble() + 0.5, pos.y.toDouble() + 1.0, pos.z.toDouble() + 0.5) {

    val particleTrail: Deque<Double3> = LinkedList<Double3>()
    val initialPhase = rand.nextInt(64)

    init {
        motionY = 0.1
        particleGravity = 0.0f
        sprite = RisingSoulTextures.headIcons[rand.nextInt(256)]
        maxAge = MathHelper.floor((0.6 + 0.4 * rand.nextDouble()) * Config.risingSoul.lifetime * 20.0)
    }

    override val isValid: Boolean get() = true

    override fun update() {
        val phase = (initialPhase + age) % 64
        velocity.setTo(cos[phase] * Config.risingSoul.perturb, 0.1, sin[phase] * Config.risingSoul.perturb)

        particleTrail.addFirst(currentPos.copy())
        while (particleTrail.size > Config.risingSoul.trailLength) particleTrail.removeLast()

        if (!Config.enabled) setExpired()
    }

    override fun render(worldRenderer: BufferBuilder, partialTickTime: Float) {
        var alpha = Config.risingSoul.opacity.toFloat()
        if (age > maxAge - 40) alpha *= (maxAge - age) / 40.0f

        renderParticleQuad(worldRenderer, partialTickTime,
            size = Config.risingSoul.headSize * 0.25,
            alpha = alpha
        )

        var scale = Config.risingSoul.trailSize * 0.25
        particleTrail.forEachPairIndexed { idx, current, previous ->
            scale *= Config.risingSoul.sizeDecay
            alpha *= Config.risingSoul.opacityDecay.toFloat()
            if (idx % Config.risingSoul.trailDensity == 0) renderParticleQuad(worldRenderer, partialTickTime,
                currentPos = current,
                prevPos = previous,
                size = scale,
                alpha = alpha,
                icon = RisingSoulTextures.trackIcon.icon!!
            )
        }
    }
}

object RisingSoulTextures : ResourceHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus, targetAtlas = Atlas.PARTICLES) {
    val headIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "rising_soul_$idx") }
    val trackIcon = iconStatic(BetterFoliage.MOD_ID, "soul_track")

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${headIcons.num} soul particle textures")
    }
}