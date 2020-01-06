package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.texture.GrassRegistry
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.common.Int3
import mods.octarinecore.common.horizontalDirections
import mods.octarinecore.common.offset
import net.minecraft.tags.BlockTags

class RenderConnectedGrass : RenderDecorator(BetterFoliageMod.MOD_ID, BetterFoliageMod.bus) {
    override fun isEligible(ctx: CombinedContext) =
        Config.enabled && Config.connectedGrass.enabled &&
        BlockTags.DIRT_LIKE.contains(ctx.state.block) &&
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
        BlockTags.DIRT_LIKE.contains(ctx.state.block) &&
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