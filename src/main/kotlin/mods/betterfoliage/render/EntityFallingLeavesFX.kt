package mods.betterfoliage.render

import com.mojang.blaze3d.vertex.IVertexBuilder
import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.config.Config
import mods.betterfoliage.render.old.AbstractEntityFX
import mods.betterfoliage.model.HSB
import mods.betterfoliage.model.getActualRenderModel
import mods.betterfoliage.render.particle.AbstractParticle
import mods.betterfoliage.texture.LeafParticleKey
import mods.betterfoliage.texture.LeafParticleRegistry
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.PI2
import mods.betterfoliage.util.get
import mods.betterfoliage.util.minmax
import mods.betterfoliage.util.randomB
import mods.betterfoliage.util.randomD
import mods.betterfoliage.util.randomF
import mods.betterfoliage.util.randomI
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.IParticleRenderType
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.Random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val rotationFactor = PI2.toFloat() / 64.0f

