package mods.betterfoliage.render.lighting

import mods.betterfoliage.chunk.BlockCtx
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader

data class LightingData(
    @JvmField var packedLight: Int = 0,
    @JvmField var colorMultiplier: Float = 1.0f
) {
    fun mixFrom(corner: LightingData, side1: LightingData, side2: LightingData, center: LightingData) {
        colorMultiplier =
            (center.colorMultiplier + side1.colorMultiplier + side2.colorMultiplier + corner.colorMultiplier) * 0.25f
        packedLight = (
                center.packedLight +
                (side1.packedLight.takeUnless { it == 0 } ?: center.packedLight) +
                (side2.packedLight.takeUnless { it == 0 } ?: center.packedLight) +
                (corner.packedLight.takeUnless { it == 0 } ?: center.packedLight)
            ).let { sum -> (sum shr 2) and 0xFF00FF }
    }
}

/**
 * Replacement for [BlockModelRenderer.AmbientOcclusionFace]
 * This gets called on a LOT, so object instantiation is avoided.
 * Not thread-safe, always use a [ThreadLocal] instance
 */
class VanillaAoCalculator {
    lateinit var world: ILightReader

    /** [blockPos] is used to get block-related information (i.e. tint, opacity, etc.)
     *  [lightPos] is used to get light-related information
     *  this facilitates masquerade rendering of blocks */
    lateinit var blockPos: BlockPos
    lateinit var lightPos: BlockPos

    private val probe = LightProbe(BlockModelRenderer.CACHE_COMBINED_LIGHT.get())

    val isValid = BooleanArray(6)
    val aoData = Array(24) { LightingData() }

    // scratchpad values used during calculation
    private val centerAo = LightingData()
    private val sideAo = Array(4) { LightingData() }
    private val cornerAo = Array(4) { LightingData() }
    private val isOccluded = BooleanArray(4)

    fun reset(ctx: BlockCtx) {
        world = ctx.world; blockPos = ctx.pos; lightPos = ctx.pos
        (0 until 6).forEach { isValid[it] = false }
    }

    fun fillLightData(lightFace: Direction, isOpaque: Boolean? = null) {
        if (!isValid[lightFace.ordinal]) calculate(lightFace, isOpaque)
    }

    /**
     * Replicate [BlockModelRenderer.AmbientOcclusionFace.updateVertexBrightness]
     * Does not handle interpolation for non-cubic models, that should be
     * done in a [VanillaVertexLighter]
     * @param lightFace face of the block to calculate
     * @param forceFull force full-block status for lighting calculation, null for auto
     */
    private fun calculate(lightFace: Direction, forceFull: Boolean?) {
        if (isValid[lightFace.ordinal]) return
        val sideHelper = AoSideHelper.forSide[lightFace.ordinal]

        // Bit 0 of the bitset in vanilla calculations
        // true if the block model is planar with the block boundary
        val isFullBlock = forceFull ?: world.getBlockState(blockPos).isCollisionShapeOpaque(world, blockPos)

        val lightOrigin = if (isFullBlock) lightPos.offset(lightFace) else lightPos

        // AO calculation for the face center
        probe.position { setPos(lightOrigin) }.writeTo(centerAo)
        if (!isFullBlock && !probe.position { move(lightFace) }.state.isOpaqueCube(world, probe.pos)) {
            // if the neighboring block in the lightface direction is
            // transparent (non-opaque), use its packed light instead of our own
            // (if our block is a full block, we are already using this value)
            centerAo.packedLight = probe.packedLight
        }

        // AO calculation for the 4 sides
        sideHelper.sides.forEachIndexed { sideIdx, sideDir ->
            // record light data in the block 1 step to the side
            probe.position { setPos(lightOrigin).move(sideDir) }.writeTo(sideAo[sideIdx])
            // side is considered occluded if the block 1 step to that side and
            // 1 step forward (in the lightface direction) is not fully transparent
            isOccluded[sideIdx] = probe.position { move(lightFace) }.isNonTransparent
        }

        // AO Calculation for the 4 corners
        AoSideHelper.faceCornersIdx.forEachIndexed { cornerIdx, sideIndices ->
            val bothOccluded = isOccluded[sideIndices.first] && isOccluded[sideIndices.second]
            if (bothOccluded) cornerAo[cornerIdx].apply {
                // if both sides are occluded, just use the packed light for one of the sides instead
                val copyFrom = sideAo[sideIndices.first]
                packedLight = copyFrom.packedLight; colorMultiplier = copyFrom.colorMultiplier
            }
            else {
                // lookup actual packed light from the cornering block in the world
                probe.position {
                    setPos(lightOrigin)
                        .move(sideHelper.sides[sideIndices.first])
                        .move(sideHelper.sides[sideIndices.second])
                }.writeTo(cornerAo[cornerIdx])
            }
        }

        // Calculate and store final interpolated value for each corner
        AoSideHelper.faceCornersIdx.forEachIndexed { cornerIdx, sideIndices ->
            val aoIdx = sideHelper.aoIndex[cornerIdx]
            aoData[aoIdx].mixFrom(
                cornerAo[cornerIdx],
                sideAo[sideIndices.first],
                sideAo[sideIndices.second],
                centerAo
            )
        }
        isValid[lightFace.ordinal] = true
    }

    inner class LightProbe(
        val cache: BlockModelRenderer.Cache
    ) {
        lateinit var state: BlockState
        val pos = BlockPos.Mutable()

        val packedLight: Int get() = cache.getPackedLight(state, world, pos)
        val colorMultiplier: Float get() = cache.getBrightness(state, world, pos)
        val isNonTransparent: Boolean get() = state.getOpacity(world, pos) > 0

        fun writeTo(data: LightingData) {
            data.packedLight = packedLight
            data.colorMultiplier = colorMultiplier
        }

        inline fun position(func: BlockPos.Mutable.() -> Unit): LightProbe {
            pos.func()
            state = world.getBlockState(pos)
            return this
        }
    }
}