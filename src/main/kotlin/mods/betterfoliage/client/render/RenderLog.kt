package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.ShadingContext
import mods.octarinecore.client.render.blockContext
import mods.octarinecore.common.Int3
import mods.octarinecore.common.rotate
import mods.octarinecore.tryDefault
import net.minecraft.block.BlockLog
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.*

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        ctx.cameraDistance < Config.roundLogs.distance &&
        Config.blocks.logs.matchesID(ctx.block)

    override var axisFunc = { state: IBlockState ->
        val axis = tryDefault(null) { state.getValue(BlockLog.LOG_AXIS).toString() } ?:
            state.properties.entries.find { it.key.getName().toLowerCase() == "axis" }?.let { it.value.toString() }
        when (axis) {
            "x" -> Axis.X
            "y" -> Axis.Y
            "z" -> Axis.Z
            else -> if (Config.roundLogs.defaultY) Axis.Y else null
        }
    }

    val columnTextures = object : ColumnTextures(Config.blocks.logs) {
        init {
            matchClassAndModel(matcher, "plantmegapack:block/_cube_column", listOf("end", "end", "side"))
            matchClassAndModel(matcher, "plantmegapack:block/_column_side", listOf("end", "end", "side"))
            matchClassAndModel(matcher, "cookingplus:block/palmlog", listOf("top", "top", "texture"))
        }
    }

    override val blockPredicate = { state: IBlockState -> Config.blocks.logs.matchesID(state.block) }
    override val surroundPredicate = { state: IBlockState -> state.isOpaqueCube && !Config.blocks.logs.matchesID(state.block) }

    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall

    override val downTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.bottomTexture?.let { base ->
            OptifineCTM.override(base, blockContext, DOWN.rotate(ctx.rotation))
        }
    }

    override val sideTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.sideTexture?.let { base ->
            OptifineCTM.override(base, blockContext, (if ((idx and 1) == 0) SOUTH else EAST).rotate(ctx.rotation))
        }
    }

    override val upTexture = { ctx: ShadingContext, idx: Int, quad: Quad ->
        columnTextures[ctx.blockData(Int3.zero).state]?.topTexture?.let { base ->
            OptifineCTM.override(base, blockContext, UP.rotate(ctx.rotation))
        }
    }
}