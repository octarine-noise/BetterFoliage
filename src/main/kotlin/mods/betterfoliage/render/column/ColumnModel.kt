package mods.betterfoliage.render.column

import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.model.HalfBakedSpecialWrapper
import mods.betterfoliage.model.SpecialRenderModel
import mods.betterfoliage.render.column.ColumnLayerData.NormalRender
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.NONSOLID
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.PARALLEL
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.PERPENDICULAR
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.INVISIBLE
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.LARGE_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SMALL_RADIUS
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.SQUARE
import mods.betterfoliage.render.lighting.ColumnLighting
import mods.betterfoliage.render.pipeline.RenderCtxBase
import net.minecraft.client.renderer.RenderType
import net.minecraft.util.Direction.Axis

abstract class ColumnModelBase(
    wrapped: SpecialRenderModel
) : HalfBakedSpecialWrapper(wrapped) {

    abstract val enabled: Boolean
    abstract val overlayLayer: ColumnRenderLayer
    abstract val connectPerpendicular: Boolean
    abstract fun getMeshSet(axis: Axis, quadrant: Int): ColumnMeshSet

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        if (!enabled) return super.renderLayer(ctx, data, layer)
        
        val roundLog = overlayLayer[ctx]
        when(roundLog) {
            ColumnLayerData.SkipRender -> return
            NormalRender -> return super.renderLayer(ctx, data, layer)
            ColumnLayerData.ResolveError, null -> {
                return super.renderLayer(ctx, data, layer)
            }
        }

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        if ((roundLog as ColumnLayerData.SpecialRender).column.axis == null && !overlayLayer.defaultToY) {
            return super.renderLayer(ctx, data, layer)
        }

        ctx.vertexLighter = ColumnLighting

        val axis = roundLog.column.axis ?: Axis.Y
        val baseRotation = ColumnMeshSet.baseRotation(axis)
        ColumnMeshSet.quadrantRotations.forEachIndexed { idx, quadrantRotation ->
            // set rotation for the current quadrant
            val rotation = baseRotation + quadrantRotation
            val meshSet = getMeshSet(axis, idx)

            // disallow sharp discontinuities in the chamfer radius, or tapering-in where inappropriate
            if (roundLog.quadrants[idx] == LARGE_RADIUS &&
                roundLog.upType == PARALLEL && roundLog.quadrantsTop[idx] != LARGE_RADIUS &&
                roundLog.downType == PARALLEL && roundLog.quadrantsBottom[idx] != LARGE_RADIUS) {
                roundLog.quadrants[idx] = SMALL_RADIUS
            }

            // select meshes for current quadrant based on connectivity rules
            val sideMesh = when (roundLog.quadrants[idx]) {
                SMALL_RADIUS -> meshSet.sideRoundSmall[idx]
                LARGE_RADIUS -> if (roundLog.upType == PARALLEL && roundLog.quadrantsTop[idx] == SMALL_RADIUS) meshSet.transitionTop[idx]
                else if (roundLog.downType == PARALLEL && roundLog.quadrantsBottom[idx] == SMALL_RADIUS) meshSet.transitionBottom[idx]
                else meshSet.sideRoundLarge[idx]
                SQUARE -> meshSet.sideSquare[idx]
                else -> null
            }

            val upMesh = when(roundLog.upType) {
                NONSOLID -> meshSet.flatTop(roundLog.quadrants, idx)
                PERPENDICULAR -> {
                    if (!connectPerpendicular) {
                        meshSet.flatTop(roundLog.quadrants, idx)
                    } else {
                        meshSet.extendTop(roundLog.quadrants, idx)
                    }
                }
                PARALLEL -> {
                    if (roundLog.quadrants[idx] discontinuousWith roundLog.quadrantsTop[idx] &&
                        roundLog.quadrants[idx].let { it == SQUARE || it == INVISIBLE } )
                        meshSet.flatTop(roundLog.quadrants, idx)
                    else null
                }
                else -> null
            }

            val downMesh = when(roundLog.downType) {
                NONSOLID -> meshSet.flatBottom(roundLog.quadrants, idx)
                PERPENDICULAR -> {
                    if (!connectPerpendicular) {
                        meshSet.flatBottom(roundLog.quadrants, idx)
                    } else {
                        meshSet.extendBottom(roundLog.quadrants, idx)
                    }
                }
                PARALLEL -> {
                    if (roundLog.quadrants[idx] discontinuousWith roundLog.quadrantsBottom[idx] &&
                        roundLog.quadrants[idx].let { it == SQUARE || it == INVISIBLE } )
                        meshSet.flatBottom(roundLog.quadrants, idx)
                    else null
                }
                else -> null
            }

            // render
            sideMesh?.let { ctx.renderQuads(it) }
            upMesh?.let { ctx.renderQuads(it) }
            downMesh?.let { ctx.renderQuads(it) }
        }

    }
}