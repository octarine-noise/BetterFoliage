package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.octarinecore.*
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.metaprog.allAvailable
import mods.octarinecore.metaprog.reflectField
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.Level

/**
 * Integration for OptiFine custom block colors.
 */
@Suppress("UNCHECKED_CAST")
object OptifineCustomColors {

    val isColorAvailable = allAvailable(CustomColors, CustomColors.getColorMultiplier)

    init {
        BetterFoliage.log(Level.INFO, "Optifine custom color support is ${if (isColorAvailable) "enabled" else "disabled" }")
    }

    val renderEnv by ThreadLocalDelegate { OptifineRenderEnv() }
    val fakeQuad = BakedQuad(IntArray(0), 1, UP, null, true, DefaultVertexFormats.BLOCK)

    fun getBlockColor(ctx: CombinedContext): Int {
        val ofColor = if (isColorAvailable && Minecraft.getInstance().gameSettings.reflectField<Boolean>("ofCustomColors") == true) {
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