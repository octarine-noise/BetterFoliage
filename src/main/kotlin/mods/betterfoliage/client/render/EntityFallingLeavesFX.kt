package mods.betterfoliage.client.render

import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.texture.LeafRegistry
import mods.octarinecore.PI2
import mods.octarinecore.client.render.AbstractEntityFX
import mods.octarinecore.client.render.HSB
import mods.octarinecore.common.Double3
import mods.octarinecore.minmax
import mods.octarinecore.random
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11
import java.lang.Math.*
import java.util.*

class EntityFallingLeavesFX(world: World, pos: BlockPos) :
AbstractEntityFX(world, pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5) {

    companion object {
        @JvmStatic val biomeBrightnessMultiplier = 0.5f
    }

    var particleRot = rand.nextInt(64)
    var rotPositive = true
    val isMirrored = (rand.nextInt() and 1) == 1
    var wasOnGround = false

    init {
        particleMaxAge = MathHelper.floor_double(random(0.6, 1.0) * Config.fallingLeaves.lifetime * 20.0)
        motionY = -Config.fallingLeaves.speed
        particleScale = Config.fallingLeaves.size.toFloat() * 0.1f

        val state = world.getBlockState(pos)
        LeafRegistry[state, world, pos, DOWN]?.let {
            particleIcon = it.particleTextures[rand.nextInt(1024)]
            calculateParticleColor(it.averageColor, state.block.colorMultiplier(world, pos))
        }
    }

    override val isValid: Boolean get() = (particleIcon != null)

    override fun update() {
        if (rand.nextFloat() > 0.95f) rotPositive = !rotPositive
        if (particleAge > particleMaxAge - 20) particleAlpha = 0.05f * (particleMaxAge - particleAge)

        if (onGround || wasOnGround) {
            velocity.setTo(0.0, 0.0, 0.0)
            if (!wasOnGround) {
                particleAge = Math.max(particleAge, particleMaxAge - 20)
                wasOnGround = true
            }
        } else {
            velocity.setTo(cos[particleRot], 0.0, sin[particleRot]).mul(Config.fallingLeaves.perturb)
                    .add(LeafWindTracker.current).add(0.0, -1.0, 0.0).mul(Config.fallingLeaves.speed)
            particleRot = (particleRot + (if (rotPositive) 1 else -1)) and 63
        }
    }

    override fun render(worldRenderer: WorldRenderer, partialTickTime: Float) {
        if (Config.fallingLeaves.opacityHack) GL11.glDepthMask(true)
        renderParticleQuad(worldRenderer, partialTickTime, rotation = particleRot, isMirrored = isMirrored)
    }

    fun calculateParticleColor(textureAvgColor: Int, blockColor: Int) {
        val texture = HSB.fromColor(textureAvgColor)
        val block = HSB.fromColor(blockColor)

        val weightTex = texture.saturation / (texture.saturation + block.saturation)
        val weightBlock = 1.0f - weightTex

        // avoid circular average for hue for performance reasons
        // one of the color components should dominate anyway
        val particle = HSB(
            weightTex * texture.hue + weightBlock * block.hue,
            weightTex * texture.saturation + weightBlock * block.saturation,
            weightTex * texture.brightness + weightBlock * block.brightness * biomeBrightnessMultiplier
        )
        setColor(particle.asColor)
    }
}

@SideOnly(Side.CLIENT)
object LeafWindTracker {
    var random = Random()
    val target = Double3.zero
    val current = Double3.zero
    var nextChange: Long = 0

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun changeWind(world: World) {
        nextChange = world.worldInfo.worldTime + 120 + random.nextInt(80)
        val direction = PI2 * random.nextDouble()
        val speed = abs(random.nextGaussian()) * Config.fallingLeaves.windStrength +
            (if (!world.isRaining) 0.0 else abs(random.nextGaussian()) * Config.fallingLeaves.stormStrength)
        target.setTo(cos(direction) * speed, 0.0, sin(direction) * speed)
    }

    @SubscribeEvent
    fun handleWorldTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) Minecraft.getMinecraft().theWorld?.let { world ->
            // change target wind speed
            if (world.worldInfo.worldTime >= nextChange) changeWind(world)

            // change current wind speed
            val changeRate = if (world.isRaining) 0.015 else 0.005
            current.add(
                (target.x - current.x).minmax(-changeRate, changeRate),
                0.0,
                (target.z - current.z).minmax(-changeRate, changeRate)
            )
        }
    }

    @SubscribeEvent
    fun handleWorldLoad(event: WorldEvent.Load) { if (event.world.isRemote) changeWind(event.world) }
}