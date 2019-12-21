package mods.betterfoliage.client.render

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.chunk.ChunkOverlayLayer
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.betterfoliage.client.integration.ShadersModIntegration.renderAs
import mods.betterfoliage.client.render.ColumnLayerData.SpecialRender.BlockType.*
import mods.betterfoliage.client.render.ColumnLayerData.SpecialRender.QuadrantType
import mods.betterfoliage.client.render.ColumnLayerData.SpecialRender.QuadrantType.*
import mods.octarinecore.client.render.*
import mods.octarinecore.common.*
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
interface IColumnTextureInfo {
    val axis: Axis?
    val top: QuadIconResolver
    val bottom: QuadIconResolver
    val side: QuadIconResolver
}

/**
 * Sealed class hierarchy for all possible render outcomes
 */
@SideOnly(Side.CLIENT)
sealed class ColumnLayerData {
    /**
     * Data structure to cache texture and world neighborhood data relevant to column rendering
     */
    @Suppress("ArrayInDataClass") // not used in comparisons anywhere
    @SideOnly(Side.CLIENT)
    data class SpecialRender(
        val column: IColumnTextureInfo,
        val upType: BlockType,
        val downType: BlockType,
        val quadrants: Array<QuadrantType>,
        val quadrantsTop: Array<QuadrantType>,
        val quadrantsBottom: Array<QuadrantType>
    ) : ColumnLayerData() {
        enum class BlockType { SOLID, NONSOLID, PARALLEL, PERPENDICULAR }
        enum class QuadrantType { SMALL_RADIUS, LARGE_RADIUS, SQUARE, INVISIBLE }
    }

    /** Column block should not be rendered at all */
    @SideOnly(Side.CLIENT)
    object SkipRender : ColumnLayerData()

    /** Column block must be rendered normally */
    @SideOnly(Side.CLIENT)
    object NormalRender : ColumnLayerData()

    /** Error while resolving render data, column block must be rendered normally */
    @SideOnly(Side.CLIENT)
    object ResolveError : ColumnLayerData()
}

@SideOnly(Side.CLIENT)
interface IColumnRegistry {
    operator fun get(state: IBlockState, rand: Int): IColumnTextureInfo?
}

@SideOnly(Side.CLIENT)
data class StaticColumnInfo(override val axis: Axis?,
                            val topTexture: TextureAtlasSprite,
                            val bottomTexture: TextureAtlasSprite,
                            val sideTextures: List<TextureAtlasSprite>) : IColumnTextureInfo {

    // index offsets for EnumFacings, to make it less likely for neighboring faces to get the same bark texture
    val dirToIdx = arrayOf(0, 1, 2, 4, 3, 5)

    override val top: QuadIconResolver = { _, _, _ -> topTexture }
    override val bottom: QuadIconResolver = { _, _, _ -> bottomTexture }
    override val side: QuadIconResolver = { ctx, idx, _ ->
        val worldFace = (if ((idx and 1) == 0) SOUTH else EAST).rotate(ctx.rotation)
        sideTextures[(blockContext.random(1) + dirToIdx[worldFace.ordinal]) % sideTextures.size]
    }
}

/** Index of SOUTH-EAST quadrant. */
const val SE = 0
/** Index of NORTH-EAST quadrant. */
const val NE = 1
/** Index of NORTH-WEST quadrant. */
const val NW = 2
/** Index of SOUTH-WEST quadrant. */
const val SW = 3

@SideOnly(Side.CLIENT)
@Suppress("NOTHING_TO_INLINE")
abstract class AbstractRenderColumn(modId: String) : AbstractBlockRenderingHandler(modId) {

    /** The rotations necessary to bring the models in position for the 4 quadrants */
    val quadrantRotations = Array(4) { Rotation.rot90[UP.ordinal] * it }

    // ============================
    // Configuration
    // ============================
    abstract val overlayLayer: ColumnRenderLayer
    abstract val connectPerpendicular: Boolean
    abstract val radiusSmall: Double
    abstract val radiusLarge: Double

    // ============================
    // Models
    // ============================
    val sideSquare = model { columnSideSquare(-0.5, 0.5) }
    val sideRoundSmall = model { columnSide(radiusSmall, -0.5, 0.5) }
    val sideRoundLarge = model { columnSide(radiusLarge, -0.5, 0.5) }

    val extendTopSquare = model { columnSideSquare(0.5, 0.5 + radiusLarge, topExtension(radiusLarge)) }
    val extendTopRoundSmall = model { columnSide(radiusSmall, 0.5, 0.5 + radiusLarge, topExtension(radiusLarge)) }
    val extendTopRoundLarge = model { columnSide(radiusLarge, 0.5, 0.5 + radiusLarge, topExtension(radiusLarge)) }
    inline fun extendTop(type: QuadrantType) = when(type) {
        SMALL_RADIUS -> extendTopRoundSmall.model
        LARGE_RADIUS -> extendTopRoundLarge.model
        SQUARE -> extendTopSquare.model
        INVISIBLE -> extendTopSquare.model
        else -> null
    }

    val extendBottomSquare = model { columnSideSquare(-0.5 - radiusLarge, -0.5, bottomExtension(radiusLarge)) }
    val extendBottomRoundSmall = model { columnSide(radiusSmall, -0.5 - radiusLarge, -0.5, bottomExtension(radiusLarge)) }
    val extendBottomRoundLarge = model { columnSide(radiusLarge, -0.5 - radiusLarge, -0.5, bottomExtension(radiusLarge)) }
    inline fun extendBottom(type: QuadrantType) = when (type) {
        SMALL_RADIUS -> extendBottomRoundSmall.model
        LARGE_RADIUS -> extendBottomRoundLarge.model
        SQUARE -> extendBottomSquare.model
        INVISIBLE -> extendBottomSquare.model
        else -> null
    }

    val topSquare = model { columnLidSquare() }
    val topRoundSmall = model { columnLid(radiusSmall) }
    val topRoundLarge = model { columnLid(radiusLarge) }
    inline fun flatTop(type: QuadrantType) = when(type) {
        SMALL_RADIUS -> topRoundSmall.model
        LARGE_RADIUS -> topRoundLarge.model
        SQUARE -> topSquare.model
        INVISIBLE -> topSquare.model
        else -> null
    }

    val bottomSquare = model { columnLidSquare() { it.rotate(rot(EAST) * 2 + rot(UP)).mirrorUV(true, true) } }
    val bottomRoundSmall = model { columnLid(radiusSmall) { it.rotate(rot(EAST) * 2 + rot(UP)).mirrorUV(true, true) } }
    val bottomRoundLarge = model { columnLid(radiusLarge) { it.rotate(rot(EAST) * 2 + rot(UP)).mirrorUV(true, true) } }
    inline fun flatBottom(type: QuadrantType) = when(type) {
        SMALL_RADIUS -> bottomRoundSmall.model
        LARGE_RADIUS -> bottomRoundLarge.model
        SQUARE -> bottomSquare.model
        INVISIBLE -> bottomSquare.model
        else -> null
    }

    val transitionTop = model { mix(sideRoundLarge.model, sideRoundSmall.model) { it > 1 } }
    val transitionBottom = model { mix(sideRoundSmall.model, sideRoundLarge.model) { it > 1 } }

    inline fun continuous(q1: QuadrantType, q2: QuadrantType) =
        q1 == q2 || ((q1 == SQUARE || q1 == INVISIBLE) && (q2 == SQUARE || q2 == INVISIBLE))

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, layer: BlockRenderLayer): Boolean {

        val roundLog = ChunkOverlayManager.get(overlayLayer, ctx.world!!, ctx.pos)
        when(roundLog) {
            ColumnLayerData.SkipRender -> return true
            ColumnLayerData.NormalRender -> return renderWorldBlockBase(ctx, dispatcher, renderer, null)
            ColumnLayerData.ResolveError, null -> {
                Client.logRenderError(ctx.blockState(Int3.zero), ctx.pos)
                return renderWorldBlockBase(ctx, dispatcher, renderer, null)
            }
        }

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        if ((roundLog as ColumnLayerData.SpecialRender).column.axis == null && !overlayLayer.defaultToY) {
            return renderWorldBlockBase(ctx, dispatcher, renderer, null)
        }

        // get AO data
        modelRenderer.updateShading(Int3.zero, allFaces)

        val baseRotation = rotationFromUp[((roundLog.column.axis ?: Axis.Y) to AxisDirection.POSITIVE).face.ordinal]
        renderAs(ctx.blockState(Int3.zero), renderer) {
            quadrantRotations.forEachIndexed { idx, quadrantRotation ->
                // set rotation for the current quadrant
                val rotation = baseRotation + quadrantRotation

                // disallow sharp discontinuities in the chamfer radius, or tapering-in where inappropriate
                if (roundLog.quadrants[idx] == LARGE_RADIUS &&
                    roundLog.upType == PARALLEL && roundLog.quadrantsTop[idx] != LARGE_RADIUS &&
                    roundLog.downType == PARALLEL && roundLog.quadrantsBottom[idx] != LARGE_RADIUS) {
                    roundLog.quadrants[idx] = SMALL_RADIUS
                }

                // render side of current quadrant
                val sideModel = when (roundLog.quadrants[idx]) {
                    SMALL_RADIUS -> sideRoundSmall.model
                    LARGE_RADIUS -> if (roundLog.upType == PARALLEL && roundLog.quadrantsTop[idx] == SMALL_RADIUS) transitionTop.model
                    else if (roundLog.downType == PARALLEL && roundLog.quadrantsBottom[idx] == SMALL_RADIUS) transitionBottom.model
                    else sideRoundLarge.model
                    SQUARE -> sideSquare.model
                    else -> null
                }

                if (sideModel != null) modelRenderer.render(
                    renderer,
                    sideModel,
                    rotation,
                    icon = roundLog.column.side,
                    postProcess = noPost
                )

                // render top and bottom end of current quadrant
                var upModel: Model? = null
                var downModel: Model? = null
                var upIcon = roundLog.column.top
                var downIcon = roundLog.column.bottom
                var isLidUp = true
                var isLidDown = true

                when (roundLog.upType) {
                    NONSOLID -> upModel = flatTop(roundLog.quadrants[idx])
                    PERPENDICULAR -> {
                        if (!connectPerpendicular) {
                            upModel = flatTop(roundLog.quadrants[idx])
                        } else {
                            upIcon = roundLog.column.side
                            upModel = extendTop(roundLog.quadrants[idx])
                            isLidUp = false
                        }
                    }
                    PARALLEL -> {
                        if (!continuous(roundLog.quadrants[idx], roundLog.quadrantsTop[idx])) {
                            if (roundLog.quadrants[idx] == SQUARE || roundLog.quadrants[idx] == INVISIBLE) {
                                upModel = topSquare.model
                            }
                        }
                    }
                }
                when (roundLog.downType) {
                    NONSOLID -> downModel = flatBottom(roundLog.quadrants[idx])
                    PERPENDICULAR -> {
                        if (!connectPerpendicular) {
                            downModel = flatBottom(roundLog.quadrants[idx])
                        } else {
                            downIcon = roundLog.column.side
                            downModel = extendBottom(roundLog.quadrants[idx])
                            isLidDown = false
                        }
                    }
                    PARALLEL -> {
                        if (!continuous(roundLog.quadrants[idx], roundLog.quadrantsBottom[idx]) &&
                            (roundLog.quadrants[idx] == SQUARE || roundLog.quadrants[idx] == INVISIBLE)) {
                            downModel = bottomSquare.model
                        }
                    }
                }

                if (upModel != null) modelRenderer.render(
                    renderer,
                    upModel,
                    rotation,
                    icon = upIcon,
                    postProcess = { _, _, _, _, _ ->
                        if (isLidUp) {
                            rotateUV(idx + if (roundLog.column.axis == Axis.X) 1 else 0)
                        }
                    }
                )
                if (downModel != null) modelRenderer.render(
                    renderer,
                    downModel,
                    rotation,
                    icon = downIcon,
                    postProcess = { _, _, _, _, _ ->
                        if (isLidDown) {
                            rotateUV((if (roundLog.column.axis == Axis.X) 0 else 3) - idx)
                        }
                    }
                )
            }
        }
        return true
    }
}

abstract class ColumnRenderLayer : ChunkOverlayLayer<ColumnLayerData> {

    abstract val registry: IColumnRegistry
    abstract val blockPredicate: (IBlockState)->Boolean
    abstract val surroundPredicate: (IBlockState) -> Boolean
    abstract val connectSolids: Boolean
    abstract val lenientConnect: Boolean
    abstract val defaultToY: Boolean

    val allNeighborOffsets = (-1..1).flatMap { offsetX -> (-1..1).flatMap { offsetY -> (-1..1).map { offsetZ -> Int3(offsetX, offsetY, offsetZ) }}}

    override fun onBlockUpdate(world: IBlockAccess, pos: BlockPos) {
        allNeighborOffsets.forEach { offset -> ChunkOverlayManager.clear(this, pos + offset) }
    }

    override fun calculate(world: IBlockAccess, pos: BlockPos) = calculate(BlockContext(world, pos))

    fun calculate(ctx: BlockContext): ColumnLayerData {
        if (ctx.isSurroundedBy(surroundPredicate)) return ColumnLayerData.SkipRender
        val columnTextures = registry[ctx.blockState(Int3.zero), ctx.random(0)] ?: return ColumnLayerData.ResolveError

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        val logAxis = columnTextures.axis ?: if (defaultToY) Axis.Y else return ColumnLayerData.NormalRender

        // check log neighborhood
        val baseRotation = rotationFromUp[(logAxis to AxisDirection.POSITIVE).face.ordinal]

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
    fun Array<QuadrantType>.checkNeighbors(ctx: BlockContext, rotation: Rotation, logAxis: Axis, yOff: Int): Array<QuadrantType> {
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
    fun BlockContext.blockType(rotation: Rotation, axis: Axis, offset: Int3): ColumnLayerData.SpecialRender.BlockType {
        val offsetRot = offset.rotate(rotation)
        val state = blockState(offsetRot)
        return if (!blockPredicate(state)) {
            if (state.isOpaqueCube) SOLID else NONSOLID
        } else {
            (registry[state, random(0)]?.axis ?: if (Config.roundLogs.defaultY) Axis.Y else null)?.let {
                if (it == axis) PARALLEL else PERPENDICULAR
            } ?: SOLID
        }
    }
}

