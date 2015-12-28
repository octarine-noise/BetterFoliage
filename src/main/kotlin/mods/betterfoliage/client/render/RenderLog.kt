package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.Axis
import mods.octarinecore.client.render.BlockContext
import net.minecraft.block.Block

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        ctx.cameraDistance < Config.roundLogs.distance &&
        Config.blocks.logs.matchesID(ctx.block)

    override var axisFunc = { block: Block, meta: Int -> when ((meta shr 2) and 3) {
        1 -> Axis.X
        2 -> Axis.Z
        else -> Axis.Y
    } }

    override val blockPredicate = { block: Block, meta: Int -> Config.blocks.logs.matchesID(block) }
    override val surroundPredicate = { block: Block -> block.isOpaqueCube && !Config.blocks.logs.matchesID(block) }

    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall
}