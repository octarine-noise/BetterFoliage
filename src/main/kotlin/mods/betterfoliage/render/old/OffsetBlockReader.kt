package mods.betterfoliage.render.old

import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.ILightReader

/**
 * Delegating [IBlockAccess] that fakes a _modified_ location to return values from a _target_ location.
 * All other locations are handled normally.
 *
 * @param[original] the [IBlockAccess] that is delegated to
 */
@Suppress("NOTHING_TO_INLINE", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "HasPlatformType")
open class OffsetBlockReader(open val original: IBlockReader, val modded: BlockPos, val target: BlockPos) : IBlockReader {
    inline fun actualPos(pos: BlockPos) = if (pos != null && pos.x == modded.x && pos.y == modded.y && pos.z == modded.z) target else pos

    override fun getBlockState(pos: BlockPos) = original.getBlockState(actualPos(pos))
    override fun getTileEntity(pos: BlockPos) = original.getTileEntity(actualPos(pos))
    override fun getFluidState(pos: BlockPos) = original.getFluidState(actualPos(pos))
}

@Suppress("NOTHING_TO_INLINE", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "HasPlatformType")
class OffsetEnvBlockReader(val original: ILightReader, val modded: BlockPos, val target: BlockPos) : ILightReader by original {
    inline fun actualPos(pos: BlockPos) = if (pos.x == modded.x && pos.y == modded.y && pos.z == modded.z) target else pos

    override fun getBlockState(pos: BlockPos) = original.getBlockState(actualPos(pos))
    override fun getTileEntity(pos: BlockPos) = original.getTileEntity(actualPos(pos))
    override fun getFluidState(pos: BlockPos) = original.getFluidState(actualPos(pos))
}

/**
 * Temporarily replaces the [IBlockReader] used by this [BlockContext] and the corresponding [ExtendedRenderBlocks]
 * to use an [OffsetEnvBlockReader] while executing this lambda.
 *
 * @param[modded] the _modified_ location
 * @param[target] the _target_ location
 * @param[func] the lambda to execute
 */
//inline fun <reified T> BlockContext.withOffset(modded: Int3, target: Int3, func: () -> T): T {
//    val original = reader!!
//    reader = OffsetEnvBlockReader(original, pos + modded, pos + target)
//    val result = func()
//    reader = original
//    return result
//}