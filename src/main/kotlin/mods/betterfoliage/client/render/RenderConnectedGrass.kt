package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.withOffset
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirsHorizontal
import mods.octarinecore.common.offset
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class RenderConnectedGrass : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {
    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        Config.blocks.dirt.matchesClass(ctx.block) &&
        Config.blocks.grassClasses.matchesClass(ctx.block(up1)) &&
        (Config.connectedGrass.snowEnabled || !ctx.blockState(up2).isSnow)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        // if the block sides are not visible anyway, render normally
        if (forgeDirsHorizontal.all { ctx.blockState(it.offset).isOpaqueCube }) return renderWorldBlockBase(ctx, dispatcher, renderer, layer)

        if (ctx.isSurroundedBy { it.isOpaqueCube } ) return false
        return ctx.withOffset(Int3.zero, up1) {
            ctx.withOffset(up1, up2) {
                renderWorldBlockBase(ctx, dispatcher, renderer, layer)
            }
        }
    }
}