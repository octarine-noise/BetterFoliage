package mods.betterfoliage.render

import mods.betterfoliage.*
import mods.betterfoliage.util.ThreadLocalDelegate
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.UP
import org.apache.logging.log4j.Level

/**
 * Integration for OptiFine custom block colors.
 */
/*
@Suppress("UNCHECKED_CAST")
object OptifineCustomColors {

    val isColorAvailable = allAvailable(CustomColors, CustomColors.getColorMultiplier)

    init {
        BetterFoliage.log(Level.INFO, "Optifine custom color support is ${if (isColorAvailable) "enabled" else "disabled" }")
    }

    val renderEnv by ThreadLocalDelegate { OptifineRenderEnv() }
    val fakeQuad = BakedQuad(IntArray(0), 1, UP, null)

    fun getBlockColor(ctx: CombinedContext): Int {
        val ofColor = if (isColorAvailable && MinecraftClient.getInstance().options.reflectDeclaredField<Boolean>("ofCustomColors") == true) {
            renderEnv.reset(ctx.state, ctx.pos)
            CustomColors.getColorMultiplier.invokeStatic(fakeQuad, ctx.state, ctx.world, ctx.pos, renderEnv.wrapped) as? Int
        } else null
        return if (ofColor == null || ofColor == -1) ctx.lightingCtx.color else ofColor
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

 */
