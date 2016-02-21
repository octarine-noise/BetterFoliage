package mods.betterfoliage.client.render

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractEntityFX
import mods.octarinecore.client.render.Double3
import mods.octarinecore.client.resource.ResourceHandler
import mods.octarinecore.forEachPairIndexed
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import org.apache.logging.log4j.Level.*
import java.util.*

class EntityRisingSoulFX(world: World, x: Int, y: Int, z: Int) :
AbstractEntityFX(world, x.toDouble() + 0.5, y.toDouble() + 1.0, z.toDouble() + 0.5) {

    val particleTrail: Deque<Double3> = LinkedList<Double3>()
    val initialPhase = rand.nextInt(64)

    init {
        motionY = 0.1
        particleGravity = 0.0f
        particleIcon = RisingSoulTextures.headIcons[rand.nextInt(256)]
        particleMaxAge = MathHelper.floor_double((0.6 + 0.4 * rand.nextDouble()) * Config.risingSoul.lifetime * 20.0)
    }

    override val isValid: Boolean get() = true

    override fun update() {
        val phase = (initialPhase + particleAge) % 64
        velocity.setTo(cos[phase] * Config.risingSoul.perturb, 0.1, sin[phase] * Config.risingSoul.perturb)

        particleTrail.addFirst(currentPos.copy())
        while (particleTrail.size > Config.risingSoul.trailLength) particleTrail.removeLast()

        if (!Config.enabled) setDead()
    }

    override fun render(tessellator: Tessellator, partialTickTime: Float) {
        var alpha = Config.risingSoul.opacity
        if (particleAge > particleMaxAge - 40) alpha *= (particleMaxAge - particleAge) / 40.0f

        renderParticleQuad(tessellator, partialTickTime,
            size = Config.risingSoul.headSize * 0.25,
            alpha = alpha
        )

        var scale = Config.risingSoul.trailSize * 0.25
        particleTrail.forEachPairIndexed { idx, current, previous ->
            scale *= Config.risingSoul.sizeDecay
            alpha *= Config.risingSoul.opacityDecay
            if (idx % Config.risingSoul.trailDensity == 0) renderParticleQuad(tessellator, partialTickTime,
                currentPos = current,
                prevPos = previous,
                size = scale,
                alpha = alpha,
                icon = RisingSoulTextures.trackIcon.icon!!
            )
        }
    }
}

@SideOnly(Side.CLIENT)
object RisingSoulTextures : ResourceHandler(BetterFoliageMod.MOD_ID) {
    val headIcons = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "rising_soul_%d")
    val trackIcon = iconStatic(BetterFoliageMod.LEGACY_DOMAIN, "soul_track")

    override fun afterStitch() {
        Client.log(INFO, "Registered ${headIcons.num} soul particle textures")
    }
}