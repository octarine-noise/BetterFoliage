package mods.betterfoliage.render.block.vanillaold

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.config.Config
import mods.betterfoliage.render.DIRT_BLOCKS
import mods.betterfoliage.render.isSnow
import mods.betterfoliage.render.old.CombinedContext
import mods.betterfoliage.render.up1
import mods.betterfoliage.render.up2
import mods.betterfoliage.texture.GrassRegistry
import mods.betterfoliage.render.old.RenderDecorator
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.horizontalDirections
import mods.betterfoliage.util.offset

class RenderConnectedGrass : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {
    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        DIRT_BLOCKS.contains(ctx.state.block) &&
        GrassRegistry[ctx, up1] != null &&
        (Config.connectedGrass.snowEnabled || !ctx.state(up2).isSnow)

    override fun render(ctx: CombinedContext) {
        // if the block sides are not visible anyway, render normally
        if (horizontalDirections.none { ctx.shouldSideBeRendered(it) }) {
            ctx.render()
        } else {
            ctx.exchange(Int3.zero, up1).exchange(up1, up2).render()
        }
    }
}

class RenderConnectedGrassLog : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {

    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.roundLogs.enabled && Config.roundLogs.connectGrass &&
                DIRT_BLOCKS.contains(ctx.state.block) &&
        LogRegistry[ctx, up1] != null

    override fun render(ctx: CombinedContext) {
        val grassDir = horizontalDirections.find { GrassRegistry[ctx, it.offset] != null }
        if (grassDir == null) {
            ctx.render()
        } else {
            ctx.exchange(Int3.zero, grassDir.offset).render()
        }
    }
}