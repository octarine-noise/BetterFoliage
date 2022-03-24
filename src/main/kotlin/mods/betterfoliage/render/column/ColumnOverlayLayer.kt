package mods.betterfoliage.render.column

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.chunk.ChunkOverlayLayer
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.chunk.dimType
import mods.betterfoliage.render.block.vanilla.RoundLogKey
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.NONSOLID
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.PARALLEL
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.PERPENDICULAR
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.SOLID
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.INVISIBLE
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.LARGE_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SMALL_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SQUARE
import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.Rotation
import mods.betterfoliage.util.allDirections
import mods.betterfoliage.util.face
import mods.betterfoliage.util.get
import mods.betterfoliage.util.plus
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.Direction.AxisDirection
import net.minecraft.world.WorldView

/** Index of SOUTH-EAST quadrant. */
const val SE = 0
/** Index of NORTH-EAST quadrant. */
const val NE = 1
/** Index of NORTH-WEST quadrant. */
const val NW = 2
/** Index of SOUTH-WEST quadrant. */
const val SW = 3

interface ColumnBlockKey {
    val axis: Axis?
}

/**
 * Sealed class hierarchy for all possible render outcomes
 */
sealed class ColumnLayerData {
    /**
     * Data structure to cache texture and world neighborhood data relevant to column rendering
     */
    @Suppress("ArrayInDataClass") // not used in comparisons anywhere
    data class SpecialRender(
        val column: ColumnBlockKey,
        val upType: BlockType,
        val downType: BlockType,
        val quadrants: Array<QuadrantType>,
        val quadrantsTop: Array<QuadrantType>,
        val quadrantsBottom: Array<QuadrantType>
    ) : ColumnLayerData() {
        enum class BlockType { SOLID, NONSOLID, PARALLEL, PERPENDICULAR }
        enum class QuadrantType {
            SMALL_RADIUS, LARGE_RADIUS, SQUARE, INVISIBLE;
            infix fun continuousWith(other: QuadrantType) =
                this == other || ((this == SQUARE || this == INVISIBLE) && (other == SQUARE || other == INVISIBLE))
            infix fun discontinuousWith(other: QuadrantType) = !continuousWith(other)
        }
    }

    /** Column block should not be rendered at all */
    object SkipRender : ColumnLayerData()

    /** Column block must be rendered normally */
    object NormalRender : ColumnLayerData()

    /** Error while resolving render data, column block must be rendered normally */
    object ResolveError : ColumnLayerData()
}

abstract class ColumnRenderLayer : ChunkOverlayLayer<ColumnLayerData> {

    abstract val connectSolids: Boolean
    abstract val lenientConnect: Boolean
    abstract val defaultToY: Boolean

    abstract fun getColumnKey(state: BlockState): ColumnBlockKey?

    val allNeighborOffsets = (-1..1).flatMap { offsetX -> (-1..1).flatMap { offsetY -> (-1..1).map { offsetZ -> Int3(offsetX, offsetY, offsetZ) }}}

    override fun onBlockUpdate(world: WorldView, pos: BlockPos) {
        allNeighborOffsets.forEach { offset -> world.dimType?.let { ChunkOverlayManager.clear(it, this, pos + offset) } }
    }

    override fun calculate(ctx: BlockCtx): ColumnLayerData {
        if (allDirections.all { dir ->
                ctx.offset(dir).let { it.isNormalCube && !BetterFoliage.blockTypes.hasTyped<RoundLogKey>(it.state) }
        }) return ColumnLayerData.SkipRender
        val columnTextures = getColumnKey(ctx.state) ?: return ColumnLayerData.ResolveError

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        val logAxis = columnTextures.axis ?: if (defaultToY) Axis.Y else return ColumnLayerData.NormalRender

        // check log neighborhood
        val baseRotation = Rotation.fromUp[(logAxis to AxisDirection.POSITIVE).face.ordinal]

        val upType = ctx.blockType(baseRotation, logAxis, Int3(0, 1, 0))
        val downType = ctx.blockType(baseRotation, logAxis, Int3(0, -1, 0))

        val quadrants = Array(4) { SMALL_RADIUS }.checkNeighbors(ctx, baseRotation, logAxis, 0)
        val quadrantsTop = Array(4) { SMALL_RADIUS }
        if (upType == PARALLEL) quadrantsTop.checkNeighbors(ctx, baseRotation, logAxis, 1)
        val quadrantsBottom = Array(4) { SMALL_RADIUS }
        if (downType == PARALLEL) quadrantsBottom.checkNeighbors(ctx, baseRotation, logAxis, -1)
        return ColumnLayerData.SpecialRender(columnTextures, upType, downType, quadrants, quadrantsTop, quadrantsBottom)
    }

    /** Sets the type of the given quadrant only if the new value is "stronger" (larger ordinal). */
    inline fun Array<QuadrantType>.upgrade(idx: Int, value: QuadrantType) {
        if (this[idx].ordinal < value.ordinal) this[idx] = value
    }

    /** Fill the array of [QuadrantType]s based on the blocks to the sides of this one. */
    fun Array<QuadrantType>.checkNeighbors(ctx: BlockCtx, rotation: Rotation, logAxis: Axis, yOff: Int): Array<QuadrantType> {
        val blkS = ctx.blockType(rotation, logAxis, Int3(0, yOff, 1))
        val blkE = ctx.blockType(rotation, logAxis, Int3(1, yOff, 0))
        val blkN = ctx.blockType(rotation, logAxis, Int3(0, yOff, -1))
        val blkW = ctx.blockType(rotation, logAxis, Int3(-1, yOff, 0))

        // a solid block on one side will make the 2 neighboring quadrants SQUARE
        // if there are solid blocks to both sides of a quadrant, it is INVISIBLE
        if (connectSolids) {
            if (blkS == SOLID) {
                upgrade(SW, SQUARE); upgrade(SE, SQUARE)
            }
            if (blkE == SOLID) {
                upgrade(SE, SQUARE); upgrade(NE, SQUARE)
            }
            if (blkN == SOLID) {
                upgrade(NE, SQUARE); upgrade(NW, SQUARE)
            }
            if (blkW == SOLID) {
                upgrade(NW, SQUARE); upgrade(SW, SQUARE)
            }
            if (blkS == SOLID && blkE == SOLID) upgrade(SE, INVISIBLE)
            if (blkN == SOLID && blkE == SOLID) upgrade(NE, INVISIBLE)
            if (blkN == SOLID && blkW == SOLID) upgrade(NW, INVISIBLE)
            if (blkS == SOLID && blkW == SOLID) upgrade(SW, INVISIBLE)
        }
        val blkSE = ctx.blockType(rotation, logAxis, Int3(1, yOff, 1))
        val blkNE = ctx.blockType(rotation, logAxis, Int3(1, yOff, -1))
        val blkNW = ctx.blockType(rotation, logAxis, Int3(-1, yOff, -1))
        val blkSW = ctx.blockType(rotation, logAxis, Int3(-1, yOff, 1))

        if (lenientConnect) {
            // if the block forms the tip of an L-shape, connect to its neighbor with SQUARE quadrants
            if (blkE == PARALLEL && (blkSE == PARALLEL || blkNE == PARALLEL)) {
                upgrade(SE, SQUARE); upgrade(NE, SQUARE)
            }
            if (blkN == PARALLEL && (blkNE == PARALLEL || blkNW == PARALLEL)) {
                upgrade(NE, SQUARE); upgrade(NW, SQUARE)
            }
            if (blkW == PARALLEL && (blkNW == PARALLEL || blkSW == PARALLEL)) {
                upgrade(NW, SQUARE); upgrade(SW, SQUARE)
            }
            if (blkS == PARALLEL && (blkSE == PARALLEL || blkSW == PARALLEL)) {
                upgrade(SW, SQUARE); upgrade(SE, SQUARE)
            }
        }

        // if the block forms the middle of an L-shape, or is part of a 2x2 configuration,
        // connect to its neighbors with SQUARE quadrants, INVISIBLE on the inner corner, and LARGE_RADIUS on the outer corner
        if (blkN == PARALLEL && blkW == PARALLEL && (lenientConnect || blkNW == PARALLEL)) {
            upgrade(SE, LARGE_RADIUS); upgrade(NE, SQUARE); upgrade(SW, SQUARE); upgrade(NW, INVISIBLE)
        }
        if (blkS == PARALLEL && blkW == PARALLEL && (lenientConnect || blkSW == PARALLEL)) {
            upgrade(NE, LARGE_RADIUS); upgrade(SE, SQUARE); upgrade(NW, SQUARE); upgrade(SW, INVISIBLE)
        }
        if (blkS == PARALLEL && blkE == PARALLEL && (lenientConnect || blkSE == PARALLEL)) {
            upgrade(NW, LARGE_RADIUS); upgrade(NE, SQUARE); upgrade(SW, SQUARE); upgrade(SE, INVISIBLE)
        }
        if (blkN == PARALLEL && blkE == PARALLEL && (lenientConnect || blkNE == PARALLEL)) {
            upgrade(SW, LARGE_RADIUS); upgrade(SE, SQUARE); upgrade(NW, SQUARE); upgrade(NE, INVISIBLE)
        }
        return this
    }

    /**
     * Get the type of the block at the given offset in a rotated reference frame.
     */
    fun BlockCtx.blockType(rotation: Rotation, axis: Axis, offset: Int3): ColumnLayerData.SpecialRender.BlockType {
        val offsetRot = offset.rotate(rotation)
        val key = getColumnKey(state(offsetRot))
        return if (key == null) {
            if (offset(offsetRot).isNormalCube) SOLID else NONSOLID
        } else {
            (key.axis ?: if (BetterFoliage.config.roundLogs.defaultY) Axis.Y else null)?.let {
                if (it == axis) PARALLEL else PERPENDICULAR
            } ?: SOLID
        }
    }
}
