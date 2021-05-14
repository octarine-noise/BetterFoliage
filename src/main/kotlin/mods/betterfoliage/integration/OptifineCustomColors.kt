package mods.betterfoliage.integration

import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.util.ThreadLocalDelegate
import mods.betterfoliage.util.allAvailable
import mods.betterfoliage.util.reflectField
import mods.octarinecore.BlockPos
import mods.octarinecore.BlockState
import mods.octarinecore.CustomColors
import mods.octarinecore.RenderEnv
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.level.ColorResolver
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.LogManager

/**
 * Integration for OptiFine custom block colors.
 */
@Suppress("UNCHECKED_CAST")
object OptifineCustomColors {
    val logger = LogManager.getLogger(this)

    val isColorAvailable = allAvailable(CustomColors, CustomColors.getColorMultiplier)

    init {
        logger.log(INFO, "Optifine custom color support is ${if (isColorAvailable) "enabled" else "disabled" }")
    }

    val renderEnv by ThreadLocalDelegate { OptifineRenderEnv() }
    val fakeQuad = BakedQuad(IntArray(0), 1, UP, null, true)

    fun getBlockColor(ctx: BlockCtx, resolver: ColorResolver): Int {
        val ofColor = if (isColorAvailable && Minecraft.getInstance().options.reflectField<Boolean>("ofCustomColors") == true) {
            renderEnv.reset(ctx.state, ctx.pos)
            CustomColors.getColorMultiplier.invokeStatic(fakeQuad, ctx.state, ctx.world, ctx.pos, renderEnv.wrapped) as? Int
        } else null
        return if (ofColor == null || ofColor == -1) ctx.color(resolver) else ofColor
    }
}

class OptifineRenderEnv {
    val wrapped: Any = RenderEnv.element!!.getDeclaredConstructor(BlockState.element, BlockPos.element).let {
        it.isAccessible = true
        it.newInstance(null, null)
    }

    fun reset(state: BlockState, pos: BlockPos) {
        RenderEnv.reset.invoke(wrapped, state, pos)
    }
}