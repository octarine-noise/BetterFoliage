package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.OffsetBlockAccess;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.client.util.RenderUtils;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Accepts dirt blocks with wooden log on top if rounded log grass is enabled.<br/>
 *  Renders the dirt block with a grass top texture.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class RenderBlockDirtWithLogTop extends RenderBlockAOBase implements IRenderBlockDecorator {

	public static final ForgeDirection[] sides = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};
	
	public RenderBlockDirtWithLogTop() {
		skipFaces = true;
	}
	
	@Override
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return Config.logsEnabled && Config.logsConnectGrass &&
			   Config.dirt.matchesID(block) &&
			   Config.logs.matchesID(blockAccess.getBlock(x, y + 1, z));
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		ForgeDirection offset = ForgeDirection.UNKNOWN;
		
		// try to find grass block in neighborhood
		for(ForgeDirection dir : sides) if (Config.grass.matchesID(world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ))) offset = dir; 

		// render offset block at render location
		IBlockAccess originalBA = renderer.blockAccess;
		renderer.blockAccess = new OffsetBlockAccess(world, x, y, z, offset.offsetX, offset.offsetY, offset.offsetZ);
		
		Block renderBlock = renderer.blockAccess.getBlock(x, y, z);
		boolean result;
		ISimpleBlockRenderingHandler handler = RenderUtils.getRenderingHandler(renderBlock.getRenderType());
		if (handler != null) {
			result = handler.renderWorldBlock(renderer.blockAccess, x, y, z, renderBlock, renderBlock.getRenderType(), renderer);
		} else {
			result = renderer.renderStandardBlock(renderBlock, x, y, z);
		}
		
		// restore renderer to sanity
		renderer.blockAccess = originalBA;
		return result;
	}

}
