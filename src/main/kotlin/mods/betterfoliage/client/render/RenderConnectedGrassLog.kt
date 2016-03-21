package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.AbstractBlockRenderingHandler
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.withOffset
import mods.octarinecore.common.Int3
import mods.octarinecore.common.offset
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.*

class RenderConnectedGrassLog : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val grassCheckDirs = listOf(EAST, WEST, NORTH, SOUTH)

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled && Config.roundLogs.connectGrass &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        Config.blocks.logs.matchesID(ctx.block(up1))

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        val grassDir = grassCheckDirs.find {
            Config.blocks.grass.matchesID(ctx.block(it.offset))
        }

        return if (grassDir != null) {
            ctx.withOffset(Int3.zero, grassDir.offset) {
                renderWorldBlockBase(ctx, dispatcher, renderer, null)
            }
        } else {
            renderWorldBlockBase(ctx, dispatcher, renderer, null)
        }
    }
}