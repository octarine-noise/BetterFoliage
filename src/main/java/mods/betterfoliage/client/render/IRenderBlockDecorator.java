package mods.betterfoliage.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

/** Block rendering handler that is only used under certain conditions
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public interface IRenderBlockDecorator extends ISimpleBlockRenderingHandler {

	/** Initialize necessary helper values
	 */
	public void init();
	
	/**
	 * @param blockAccess the world
	 * @param x
	 * @param y
	 * @param z
	 * @param block
	 * @param original renderType of the block
	 * @return true if this renderer should handle this block
	 */
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original);
	
}
