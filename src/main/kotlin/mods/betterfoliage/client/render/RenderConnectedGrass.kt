package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.RenderBlocks

class RenderConnectedGrass : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {
    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        Config.blocks.dirt.matchesID(ctx.block) &&
        Config.blocks.grass.matchesID(ctx.block(up1)) &&
        (Config.connectedGrass.snowEnabled || !ctx.block(up2).isSnow)

    override fun render(ctx: BlockContext, parent: RenderBlocks): Boolean {
        return ctx.withOffset(Int3.zero, up1) {
            ctx.withOffset(up1, up2) {
                renderWorldBlockBase(parent, face = alwaysRender)
            }
        }
    }
}