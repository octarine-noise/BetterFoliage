package mods.betterfoliage.client.render

import mods.betterfoliage.client.config.BlockMatcher
import mods.betterfoliage.client.render.AbstractRenderColumn.BlockType.*
import mods.betterfoliage.client.render.AbstractRenderColumn.QuadrantType.*
import mods.octarinecore.client.render.*
import mods.octarinecore.client.resource.BlockTextureInspector
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.face
import mods.octarinecore.common.rot
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.EnumWorldBlockLayer

data class ColumnInfo(val topTexture: TextureAtlasSprite,
                      val bottomTexture: TextureAtlasSprite,
                      val sideTexture: TextureAtlasSprite)

open class ColumnTextures(val matcher: BlockMatcher) : BlockTextureInspector<ColumnInfo>() {
    init {
        matchClassAndModel(matcher, "block/column_side", listOf("end", "end", "side"))
        matchClassAndModel(matcher, "block/cube_column", listOf("end", "end", "side"))
        matchClassAndModel(matcher, "block/cube_all", listOf("all", "all", "all"))
    }
    override fun processTextures(state: IBlockState, textures: List<TextureAtlasSprite>, atlas: TextureMap) =
        ColumnInfo(textures[0], textures[1], textures[2])
}

/** Index of SOUTH-EAST quadrant. */
const val SE = 0
/** Index of NORTH-EAST quadrant. */
const val NE = 1
/** Index of NORTH-WEST quadrant. */
const val NW = 2
/** Index of SOUTH-WEST quadrant. */
const val SW = 3

@Suppress("NOTHING_TO_INLINE")
abstract class AbstractRenderColumn(modId: String) : AbstractBlockRenderingHandler(modId) {

    enum class BlockType { SOLID, NONSOLID, PARALLEL, PERPENDICULAR }
    enum class QuadrantType { SMALL_RADIUS, LARGE_RADIUS, SQUARE, INVISIBLE }

    /** The rotations necessary to bring the models in position for the 4 quadrants */
    val quadrantRotations = Array(4) { Rotation.rot90[UP.ordinal] * it }

    // ============================
    // Configuration
    // ============================
    abstract val radiusSmall: Double
    abstract val radiusLarge: Double
    abstract val surroundPredicate: (IBlockState) -> Boolean
    abstract val connectPerpendicular: Boolean
    abstract val connectSolids: Boolean
    abstract val lenientConnect: Boolean

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

    val bottomSquare = model { columnLidSquare() { it.rotate(rot(EAST) * 2 + rot(UP)) } }
    val bottomRoundSmall = model { columnLid(radiusSmall) { it.rotate(rot(EAST) * 2 + rot(UP)) } }
    val bottomRoundLarge = model { columnLid(radiusLarge) { it.rotate(rot(EAST) * 2 + rot(UP)) } }
    inline fun flatBottom(type: QuadrantType) = when(type) {
        SMALL_RADIUS -> bottomRoundSmall.model
        LARGE_RADIUS -> bottomRoundLarge.model
        SQUARE -> bottomSquare.model
        INVISIBLE -> bottomSquare.model
        else -> null
    }

    val transitionTop = model { mix(sideRoundLarge.model, sideRoundSmall.model) { it > 1 } }
    val transitionBottom = model { mix(sideRoundSmall.model, sideRoundLarge.model) { it > 1 } }

    inline fun continous(q1: QuadrantType, q2: QuadrantType) =
        q1 == q2 || ((q1 == SQUARE || q1 == INVISIBLE) && (q2 == SQUARE || q2 == INVISIBLE))

    abstract val axisFunc: (IBlockState)->EnumFacing.Axis?
    abstract val blockPredicate: (IBlockState)->Boolean

    abstract val sideTexture: (ShadingContext, Int, Quad)->TextureAtlasSprite?
    abstract val upTexture: (ShadingContext, Int, Quad)->TextureAtlasSprite?
    abstract val downTexture: (ShadingContext, Int, Quad)->TextureAtlasSprite?

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: WorldRenderer, layer: EnumWorldBlockLayer): Boolean {
        if (ctx.isSurroundedBy(surroundPredicate) ) return false

        // get AO data
        modelRenderer.updateShading(Int3.zero, allFaces)

        // check log neighborhood
        val logAxis = ctx.blockAxis ?: return renderWorldBlockBase(ctx, dispatcher, renderer, null)
        val baseRotation = rotationFromUp[(logAxis to AxisDirection.POSITIVE).face.ordinal]

        val upType = ctx.blockType(baseRotation, logAxis, Int3(0, 1, 0))
        val downType = ctx.blockType(baseRotation, logAxis, Int3(0, -1, 0))

        val quadrants = Array(4) { SMALL_RADIUS }.checkNeighbors(ctx, baseRotation, logAxis, 0)
        val quadrantsTop = Array(4) { SMALL_RADIUS }
        if (upType == PARALLEL) quadrantsTop.checkNeighbors(ctx, baseRotation, logAxis, 1)
        val quadrantsBottom = Array(4) { SMALL_RADIUS }
        if (downType == PARALLEL) quadrantsBottom.checkNeighbors(ctx, baseRotation, logAxis, -1)

        quadrantRotations.forEachIndexed { idx, quadrantRotation ->
            // set rotation for the current quadrant
            val rotation = baseRotation + quadrantRotation

            // disallow sharp discontinuities in the chamfer radius, or tapering-in where inappropriate
            if (quadrants[idx] == LARGE_RADIUS &&
                upType == PARALLEL && quadrantsTop[idx] != LARGE_RADIUS &&
                downType == PARALLEL && quadrantsBottom[idx] != LARGE_RADIUS) {
                quadrants[idx] = SMALL_RADIUS
            }

            // render side of current quadrant
            val sideModel = when (quadrants[idx]) {
                SMALL_RADIUS -> sideRoundSmall.model
                LARGE_RADIUS -> if (upType == PARALLEL && quadrantsTop[idx] == SMALL_RADIUS) transitionTop.model
                else if (downType == PARALLEL && quadrantsBottom[idx] == SMALL_RADIUS) transitionBottom.model
                else sideRoundLarge.model
                SQUARE -> sideSquare.model
                else -> null
            }

            if (sideModel != null) modelRenderer.render(
                renderer,
                sideModel,
                rotation,
                blockContext.blockCenter,
                icon = sideTexture,
                rotateUV = { 0 },
                postProcess = noPost
            )

            // render top and bottom end of current quadrant
            var upModel: Model? = null
            var downModel: Model? = null
            var upIcon = upTexture
            var downIcon = downTexture
            var shouldRotateUp = true
            var shouldRotateDown = true

            when (upType) {
                NONSOLID -> upModel = flatTop(quadrants[idx])
                PERPENDICULAR -> {
                    if (!connectPerpendicular) {
                        upModel = flatTop(quadrants[idx])
                    } else {
                        upIcon = sideTexture
                        upModel = extendTop(quadrants[idx])
                        shouldRotateUp = false
                    }
                }
                PARALLEL -> {
                    if (!continous(quadrants[idx], quadrantsTop[idx])) {
                        if (quadrants[idx] == SQUARE || quadrants[idx] == INVISIBLE) {
                            upModel = topSquare.model
                        }
                    }
                }
            }
            when (downType) {
                NONSOLID -> downModel = flatBottom(quadrants[idx])
                PERPENDICULAR -> {
                    if (!connectPerpendicular) {
                        downModel = flatBottom(quadrants[idx])
                    } else {
                        downIcon = sideTexture
                        downModel = extendBottom(quadrants[idx])
                        shouldRotateDown = false
                    }
                }
                PARALLEL -> {
                    if (!continous(quadrants[idx], quadrantsBottom[idx]) &&
                        (quadrants[idx] == SQUARE || quadrants[idx] == INVISIBLE)) {
                        downModel = bottomSquare.model
                    }
                }
            }

            if (upModel != null) modelRenderer.render(
                renderer,
                upModel,
                rotation,
                blockContext.blockCenter,
                icon = upIcon,
                rotateUV = { if (shouldRotateUp) idx else 0 },
                postProcess = noPost
            )
            if (downModel != null) modelRenderer.render(
                renderer,
                downModel,
                rotation,
                blockContext.blockCenter,
                icon = downIcon,
                rotateUV = { if (shouldRotateDown) 3 - idx else 0 },
                postProcess = noPost
            )
        }

        return true
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

    /** Get the axis of the block */
    val BlockContext.blockAxis: Axis? get() = axisFunc(blockState(Int3.zero))

    /**
     * Get the type of the block at the given offset in a rotated reference frame.
     */
    fun BlockContext.blockType(rotation: Rotation, axis: Axis, offset: Int3): BlockType {
        val offsetRot = offset.rotate(rotation)
        val state = blockState(offsetRot)
        return if (!blockPredicate(state)) {
            if (state.block.isOpaqueCube) SOLID else NONSOLID
        } else {
            axisFunc(state)?.let { if (it == axis) PARALLEL else PERPENDICULAR } ?: SOLID
        }
    }
}