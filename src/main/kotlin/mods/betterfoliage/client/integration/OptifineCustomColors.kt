package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.loader.Refs
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.common.Int3
import mods.octarinecore.metaprog.allAvailable
import mods.octarinecore.metaprog.reflectField
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level

/**
 * Integration for OptiFine custom block colors.
 */
@Suppress("UNCHECKED_CAST")
@SideOnly(Side.CLIENT)
object OptifineCustomColors {

    val isColorAvailable = allAvailable(
        Refs.CustomColors, Refs.getColorMultiplier
    )

    init {
        Client.log(Level.INFO, "Optifine custom color support is ${if (isColorAvailable) "enabled" else "disabled" }")
    }

    val renderEnv by ThreadLocalDelegate { OptifineRenderEnv() }
    val fakeQuad = BakedQuad(IntArray(0), 1, EnumFacing.UP, null, true, DefaultVertexFormats.BLOCK)

    fun getBlockColor(ctx: BlockContext): Int {
        val ofColor = if (isColorAvailable && Minecraft.getMinecraft().gameSettings.reflectField<Boolean>("ofCustomColors") == true) {
            renderEnv.reset(ctx.blockState(Int3.zero), ctx.pos)
            Refs.getColorMultiplier.invokeStatic(fakeQuad, ctx.blockState(Int3.zero), ctx.world!!, ctx.pos, renderEnv.wrapped) as? Int
        } else null
        return if (ofColor == null || ofColor == -1) ctx.blockData(Int3.zero).color else ofColor
    }
}

@SideOnly(Side.CLIENT)
class OptifineRenderEnv {
    val wrapped: Any = Refs.RenderEnv.element!!.getDeclaredConstructor(
        Refs.IBlockState.element, Refs.BlockPos.element
    ).let {
        it.isAccessible = true
        it.newInstance(null, null)
    }

    fun reset(state: IBlockState, pos: BlockPos) {
        Refs.RenderEnv_reset.invoke(wrapped, state, pos)
    }
}