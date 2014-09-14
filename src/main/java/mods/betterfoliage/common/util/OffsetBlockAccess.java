package mods.betterfoliage.common.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** {@link IBlockAccess} wrapper that applies an offset for a single target coordinate for all rendering-related methods.
 * Returns normal values for all other coordinates.
 * @author octarine-noise
 *
 */
public class OffsetBlockAccess implements IBlockAccess {

	public IBlockAccess source;
	public int xTarget, yTarget, zTarget;
	public int xOffset, yOffset, zOffset;

	public OffsetBlockAccess(IBlockAccess source, int x, int y, int z, int xOffset, int yOffset, int zOffset) {
		this.source = source;
		this.xTarget = x;
		this.yTarget = y;
		this.zTarget = z;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.zOffset = zOffset;
	}

	public Block getBlock(int x, int y, int z) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.getBlock(x + xOffset, y + yOffset, z + zOffset);
		else
			return source.getBlock(x, y, z);
	}

	public TileEntity getTileEntity(int x, int y, int z) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.getTileEntity(x + xOffset, y + yOffset, z + zOffset);
		else
			return source.getTileEntity(x, y, z);
	}

	@SideOnly(Side.CLIENT)
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int min) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.getLightBrightnessForSkyBlocks(x + xOffset, y + yOffset, z + zOffset, min);
		else 
			return source.getLightBrightnessForSkyBlocks(x, y, z, min);
	}

	public int getBlockMetadata(int x, int y, int z) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.getBlockMetadata(x + xOffset, y + yOffset, z + zOffset);
		else
			return source.getBlockMetadata(x, y, z);
	}

	public boolean isAirBlock(int x, int y, int z) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.isAirBlock(x + xOffset, y + yOffset, z + zOffset);
		else
			return source.isAirBlock(x, y, z);
	}

	@SideOnly(Side.CLIENT)
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return source.getBiomeGenForCoords(x, z);
	}

	@SideOnly(Side.CLIENT)
	public int getHeight() {
		return source.getHeight();
	}

	@SideOnly(Side.CLIENT)
	public boolean extendedLevelsInChunkCache() {
		return source.extendedLevelsInChunkCache();
	}

	public int isBlockProvidingPowerTo(int x, int y, int z, int dir) {
		return source.isBlockProvidingPowerTo(x, y, z, dir);
	}

	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
		if (x == xTarget && y == yTarget && z == zTarget)
			return source.isSideSolid(x + xOffset, y + yOffset, z + zOffset, side, _default);
		else
			return source.isSideSolid(x, y, z, side, _default);
	}

}
