@file:JvmName("RendererHolder")
package mods.octarinecore.client.render

import mods.betterfoliage.client.render.canRenderInCutout
import mods.betterfoliage.client.render.isCutout
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.resource.ResourceHandler
import mods.octarinecore.common.Double3
import mods.octarinecore.common.Int3
import mods.octarinecore.common.allDirOffsets
import mods.octarinecore.common.plus
import mods.octarinecore.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.color.BlockColors
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.IEnviromentBlockReader
import net.minecraft.world.biome.Biome
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.eventbus.api.IEventBus
import java.util.*
import kotlin.math.abs

abstract class RenderDecorator(modId: String, modBus: IEventBus) : ResourceHandler(modId, modBus) {

    open val renderOnCutout: Boolean get() = true
    open val onlyOnCutout: Boolean get() = false

    // ============================
    // Custom rendering
    // ============================
    abstract fun isEligible(ctx: CombinedContext): Boolean
    abstract fun render(ctx: CombinedContext)

}

data class BlockData(val state: BlockState, val color: Int, val brightness: Int)


