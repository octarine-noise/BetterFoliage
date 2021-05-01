package mods.betterfoliage.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.resource.Identifier
import mods.betterfoliage.render.old.AbstractEntityFX
import mods.betterfoliage.resource.ResourceHandler
import mods.betterfoliage.util.Atlas
import mods.betterfoliage.util.Double3
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
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
//        var alpha = Config.risingSoul.opacity.toFloat()
//        if (age > maxAge - 40) alpha *= (maxAge - age) / 40.0f
//
//        renderParticleQuad(worldRenderer, partialTickTime,
//            size = Config.risingSoul.headSize * 0.25,
//            alpha = alpha
//        )
//
//        var scale = Config.risingSoul.trailSize * 0.25
//        particleTrail.forEachPairIndexed { idx, current, previous ->
//            scale *= Config.risingSoul.sizeDecay
//            alpha *= Config.risingSoul.opacityDecay.toFloat()
//            if (idx % Config.risingSoul.trailDensity == 0) renderParticleQuad(worldRenderer, partialTickTime,
//                currentPos = current,
//                prevPos = previous,
//                size = scale,
//                alpha = alpha,
//                icon = RisingSoulTextures.trackIcon
//            )
//        }
    }
}

object RisingSoulTextures : ResourceHandler(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus, targetAtlas = Atlas.PARTICLES) {
    val headIcons = spriteSet { idx -> ResourceLocation(BetterFoliageMod.MOD_ID, "rising_soul_$idx") }
    val trackIcon by sprite(Identifier(BetterFoliageMod.MOD_ID, "soul_track"))
}