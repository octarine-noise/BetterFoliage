package mods.betterfoliage.client.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

@SideOnly(Side.CLIENT)
public interface IRenderBlockDecorator extends ISimpleBlockRenderingHandler {

	public void init();
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original);
	
}
