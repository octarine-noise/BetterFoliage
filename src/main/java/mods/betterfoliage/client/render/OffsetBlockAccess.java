package mods.betterfoliage.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;

/** {@link IBlockAccess} wrapper that applies an offset for a single target coordinate for all rendering-related methods.
 * Returns normal values for all other coordinates.
 * @author octarine-noise
 *
 */
public class OffsetBlockAccess implements IBlockAccess {

	public IBlockAccess source;
	public BlockPos target, returned;

	public OffsetBlockAccess(IBlockAccess source, BlockPos target, int xOffset, int yOffset, int zOffset) {
		this.source = source;
		this.target = target;
		this.returned = target.add(xOffset, yOffset, zOffset);
	}

	public OffsetBlockAccess(IBlockAccess source, BlockPos target, EnumFacing direction) {
		this(source, target, direction.getFrontOffsetX(), direction.getFrontOffsetY(), direction.getFrontOffsetZ());
	}
	
	protected BlockPos getPos(BlockPos original) {
		return original.equals(target) ? returned : original;
	}
	
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return source.getTileEntity(getPos(pos));
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return source.getCombinedLight(pos, lightValue);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return source.getBlockState(getPos(pos));
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return source.isAirBlock(getPos(pos));
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
		return source.getBiomeGenForCoords(pos);
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return source.extendedLevelsInChunkCache();
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return source.getStrongPower(pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return source.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return source.isSideSolid(pos, side, _default);
	}

}
