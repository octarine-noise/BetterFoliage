package mods.betterfoliage.render.column

import mods.betterfoliage.chunk.CachedBlockCtx
import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.render.column.ColumnLayerData.NormalRender
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.BlockType.*
import mods.betterfoliage.render.column.ColumnLayerData.SpecialRender.QuadrantType.*
import mods.betterfoliage.model.WrappedBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction.Axis
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

abstract class ColumnModelBase(wrapped: BakedModel) : WrappedBakedModel(wrapped) {
    abstract val enabled: Boolean
    abstract val overlayLayer: ColumnRenderLayer
    abstract val connectPerpendicular: Boolean
    abstract fun getMeshSet(axis: Axis, quadrant: Int): ColumnMeshSet

    override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
        if (!enabled) return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        val ctx = CachedBlockCtx(blockView, pos)
        val roundLog = ChunkOverlayManager.get(overlayLayer, ctx)

        when(roundLog) {
            ColumnLayerData.SkipRender -> return
            NormalRender -> return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
            ColumnLayerData.ResolveError, null -> {
                return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
            }
        }

        // if log axis is not defined and "Default to vertical" config option is not set, render normally
        if ((roundLog as ColumnLayerData.SpecialRender).column.axis == null && !overlayLayer.defaultToY) {
            return super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }

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
            sideMesh?.let { context.meshConsumer().accept(it) }
            upMesh?.let { context.meshConsumer().accept(it) }
            downMesh?.let { context.meshConsumer().accept(it) }

        }
    }
}