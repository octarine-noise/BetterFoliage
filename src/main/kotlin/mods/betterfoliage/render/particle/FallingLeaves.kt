package mods.betterfoliage.render.particle

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.ClientWorldLoadCallback
import mods.betterfoliage.render.block.vanilla.LeafParticleKey
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.minmax
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomF
import mods.betterfoliage.util.randomI
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.client.particle.ParticleTextureSheet
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class FallingLeafParticle(
    world: ClientWorld, pos: BlockPos, leaf: LeafParticleKey, blockColor: Int, random: Random
) : AbstractParticle(
    world, pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5
) {

    companion object {
        @JvmStatic val biomeBrightnessMultiplier = 0.5f
    }

    var rotationSpeed = random.randomF(min = PI2 / 80.0, max = PI2 / 50.0)
    val isMirrored = randomB()
    var wasCollided = false

    init {
        angle = random.randomF(max = PI2)
        prevAngle = angle - rotationSpeed

        maxAge = MathHelper.floor(randomD(0.6, 1.0) * BetterFoliage.config.fallingLeaves.lifetime * 20.0)
        velocityY = -BetterFoliage.config.fallingLeaves.speed

        scale = BetterFoliage.config.fallingLeaves.size.toFloat() * 0.1f

        val state = world.getBlockState(pos)

        setColor(leaf.overrideColor?.asInt ?: blockColor)
        sprite = LeafParticleRegistry[leaf.leafType][randomI(max = 1024)]
    }

    override val isValid: Boolean get() = (sprite != null)

    override fun update() {
        if (randomF() > 0.95f) rotationSpeed = -rotationSpeed
        if (age > maxAge - 20) alpha = 0.05f * (maxAge - age)

        if (onGround || wasCollided) {
            velocity.setTo(0.0, 0.0, 0.0)
            prevAngle = angle
            if (!wasCollided) {
                age = age.coerceAtLeast(maxAge - 20)
                wasCollided = true
            }
        } else {
            val cosRotation = cos(angle).toDouble(); val sinRotation = sin(angle).toDouble()
            velocity.setTo(cosRotation, 0.0, sinRotation).mul(BetterFoliage.config.fallingLeaves.perturb)
                    .add(LeafWindTracker.current).add(0.0, -1.0, 0.0).mul(BetterFoliage.config.fallingLeaves.speed)
            prevAngle = angle
            angle += rotationSpeed
        }
    }

    fun setParticleColor(overrideColor: Int?, blockColor: Int) {
        val color =  overrideColor ?: blockColor
        setColor(color)
    }

    override fun getType() =
        if (BetterFoliage.config.fallingLeaves.opacityHack) ParticleTextureSheet.PARTICLE_SHEET_OPAQUE
        else ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT
}

object LeafWindTracker : WorldTickCallback, ClientWorldLoadCallback {
    val random = Random()
    val target = Double3.zero
    val current = Double3.zero
    var nextChange: Long = 0

    fun changeWindTarget(world: World) {
        nextChange = world.time + 120 + random.nextInt(80)
        val direction = PI2 * random.nextDouble()
        val speed = abs(random.nextGaussian()) * BetterFoliage.config.fallingLeaves.windStrength +
            (if (!world.isRaining) 0.0 else abs(random.nextGaussian()) * BetterFoliage.config.fallingLeaves.stormStrength)
        target.setTo(cos(direction) * speed, 0.0, sin(direction) * speed)
    }

    override fun tick(world: World) {
        if (world.isClient) {
            // change target wind speed
            if (world.time >= nextChange) changeWindTarget(world)

            // change current wind speed
            val changeRate = if (world.isRaining) 0.015 else 0.005
            current.add(
                (target.x - current.x).minmax(-changeRate, changeRate),
                0.0,
                (target.z - current.z).minmax(-changeRate, changeRate)
            )
        }
    }

    override fun loadWorld(world: ClientWorld) {
        changeWindTarget(world)
    }
}

