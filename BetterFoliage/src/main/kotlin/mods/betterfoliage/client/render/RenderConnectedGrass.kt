package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.withOffset
import mods.octarinecore.common.Int3
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer

class RenderConnectedGrass : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {
    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        Config.blocks.grass.matchesID(ctx.block(up1)) &&
        (Config.connectedGrass.snowEnabled || !ctx.blockState(up2).isSnow)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        return ctx.withOffset(Int3.zero, up1) {
            ctx.withOffset(up1, up2) {
                renderWorldBlockBase(ctx, dispatcher, renderer, null)
            }
        }
    }
}