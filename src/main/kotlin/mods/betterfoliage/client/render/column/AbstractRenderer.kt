package mods.betterfoliage.client.render.column

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.chunk.ChunkOverlayManager
import mods.betterfoliage.client.integration.ShadersModIntegration.renderAs
import mods.betterfoliage.client.render.*
import mods.betterfoliage.client.render.column.ColumnLayerData.SpecialRender.BlockType.*
import mods.betterfoliage.client.render.column.ColumnLayerData.SpecialRender.QuadrantType
import mods.betterfoliage.client.render.column.ColumnLayerData.SpecialRender.QuadrantType.*
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.Model
import mods.octarinecore.client.render.RenderDecorator
import mods.octarinecore.client.render.noPost
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.face
import mods.octarinecore.common.rot
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.util.Direction.*
import net.minecraftforge.eventbus.api.IEventBus

@Suppress("NOTHING_TO_INLINE")
abstract class AbstractRenderColumn(modId: String, modBus: IEventBus) : RenderDecorator(modId, modBus) {

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
    override fun render(ctx: CombinedContext) {

        val roundLog = ChunkOverlayManager.get(overlayLayer, ctx)
        when(roundLog) {
            ColumnLayerData.SkipRender -> return
            ColumnLayerData.NormalRender -> return ctx.render()
            ColumnLayerData.ResolveError, null -> {
                BetterFoliage.logRenderError(ctx.state, ctx.pos)
                return ctx.render()
            }
        }

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        if ((roundLog as ColumnLayerData.SpecialRender).column.axis == null && !overlayLayer.defaultToY) {
            return ctx.render()
        }

        val baseRotation = rotationFromUp[((roundLog.column.axis ?: Axis.Y) to AxisDirection.POSITIVE).face.ordinal]
        renderAs(ctx, MODEL) {
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

                if (sideModel != null) ctx.render(
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

                if (upModel != null) ctx.render(
                    upModel,
                    rotation,
                    icon = upIcon,
                    postProcess = { _, _, _, _, _ ->
                        if (isLidUp) {
                            rotateUV(idx + if (roundLog.column.axis == Axis.X) 1 else 0)
                        }
                    }
                )
                if (downModel != null) ctx.render(
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
    }
}
