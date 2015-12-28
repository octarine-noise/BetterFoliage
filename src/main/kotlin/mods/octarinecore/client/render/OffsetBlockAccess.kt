package mods.octarinecore.client.render

import mods.octarinecore.minmax
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection

/**
 * Delegating [IBlockAccess] that fakes a _modified_ location to return values from a _target_ location.
 * All other locations are handled normally.
 *
 * @param[original] the [IBlockAccess] that is delegated to
 * @param[xModded] x coordinate of the _modified_ location
 * @param[yModded] y coordinate of the _modified_ location
 * @param[zModded] z coordinate of the _modified_ location
 * @param[xTarget] x coordinate of the _target_ location
 * @param[yTarget] y coordinate of the _target_ location
 * @param[zTarget] z coordinate of the _target_ location
 */
class OffsetBlockAccess(val original: IBlockAccess,
                        @JvmField val xModded: Int, @JvmField val yModded: Int, @JvmField val zModded: Int,
                        @JvmField val xTarget: Int, @JvmField val yTarget: Int, @JvmField val zTarget: Int) : IBlockAccess {

    inline fun <reified T> withOffset(x: Int, y: Int, z: Int, func: (Int,Int,Int)->T): T {
        if (x == xModded && y == yModded && z == zModded) {
            return func(xTarget, yTarget, zTarget)
        } else {
            return func(x, y, z)
        }
    }

    override fun getBlock(x: Int, y: Int, z: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.getBlock(xAct, yAct, zAct) }
    override fun getBlockMetadata(x: Int, y: Int, z: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.getBlockMetadata(xAct, yAct, zAct) }
    override fun getTileEntity(x: Int, y: Int, z: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.getTileEntity(xAct, yAct, zAct) }
    override fun isSideSolid(x: Int, y: Int, z: Int, side: ForgeDirection?, _default: Boolean) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.isSideSolid(xAct, yAct, zAct, side, _default) }
    override fun isAirBlock(x: Int, y: Int, z: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.isAirBlock(xAct, yAct, zAct) }
    override fun getLightBrightnessForSkyBlocks(x: Int, y: Int, z: Int, side: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.getLightBrightnessForSkyBlocks(xAct, yAct, zAct, side) }
    override fun isBlockProvidingPowerTo(x: Int, y: Int, z: Int, side: Int) = withOffset(x, y, z)
        { xAct, yAct, zAct -> original.isBlockProvidingPowerTo(xAct, yAct, zAct, side) }
    override fun getBiomeGenForCoords(x: Int, z: Int) = withOffset(x, 0, z)
        { xAct, yAct, zAct -> original.getBiomeGenForCoords(xAct, zAct) }

    override fun getHeight() = original.height
    override fun extendedLevelsInChunkCache() = original.extendedLevelsInChunkCache()
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
    world = OffsetBlockAccess(original, x + modded.x, y + modded.y, z + modded.z, x + target.x, y + target.y, z + target.z)
    renderBlocks.blockAccess = world
    val result = func()
    world = original
    renderBlocks.blockAccess = original
    return result
}