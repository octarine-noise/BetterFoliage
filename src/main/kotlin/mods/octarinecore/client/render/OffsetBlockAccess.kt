package mods.octarinecore.client.render

import mods.octarinecore.common.Int3
import mods.octarinecore.common.plus
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

/**
 * Delegating [IBlockAccess] that fakes a _modified_ location to return values from a _target_ location.
 * All other locations are handled normally.
 *
 * @param[original] the [IBlockAccess] that is delegated to
 */
@Suppress("NOTHING_TO_INLINE")
class OffsetBlockAccess(val original: IBlockAccess, val modded: BlockPos, val target: BlockPos) : IBlockAccess {

    inline fun actualPos(pos: BlockPos?) =
        if (pos != null && pos.x == modded.x && pos.y == modded.y && pos.z == modded.z) target else pos

    override fun extendedLevelsInChunkCache() = original.extendedLevelsInChunkCache()
    override fun getBiomeGenForCoords(pos: BlockPos?) = original.getBiomeGenForCoords(actualPos(pos))
    override fun getBlockState(pos: BlockPos?) = original.getBlockState(actualPos(pos))
    override fun getCombinedLight(pos: BlockPos?, lightValue: Int) = original.getCombinedLight(actualPos(pos), lightValue)
    override fun getStrongPower(pos: BlockPos?, direction: EnumFacing?) = original.getStrongPower(actualPos(pos), direction)
    override fun getTileEntity(pos: BlockPos?) = original.getTileEntity(actualPos(pos))
    override fun getWorldType() = original.worldType
    override fun isAirBlock(pos: BlockPos?) = original.isAirBlock(actualPos(pos))
    override fun isSideSolid(pos: BlockPos?, side: EnumFacing?, _default: Boolean) = original.isSideSolid(actualPos(pos), side, _default)
}
/**
 * Temporarily replaces the [IBlockAccess] used by this [BlockContext] and the corresponding [ExtendedRenderBlocks]
 * to use an [OffsetBlockAccess] while executing this lambda.
 *
 * @param[modded] the _modified_ location
 * @param[target] the _target_ location
 * @param[func] the lambda to execute
 */
inline fun <reified T> BlockContext.withOffset(modded: Int3, target: Int3, func: () -> T): T {
    val original = world!!
    world = OffsetBlockAccess(original, pos + modded, pos + target)
    val result = func()
    world = original
    return result
}