package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.OffsetBlockAccess;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.client.util.RenderUtils;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Accepts dirt blocks with grass on top if aggressive connected grass is enabled.<br/>
 *  Renders the grass block in place of dirt.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class RenderBlockDirtWithGrassTop extends RenderBlockAOBase implements IRenderBlockDecorator {

	public RenderBlockDirtWithGrassTop() {
		skipFaces = true;
	}
	
	@Override
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
	    Material top2Material = blockAccess.getBlock(x, y + 2, z).getMaterial();
		return Config.ctxGrassAggressiveEnabled &&
		       top2Material != Material.snow &&
		       top2Material != Material.craftedSnow &&
			   Config.dirt.matchesID(block) &&
			   Config.grass.matchesID(blockAccess.getBlock(x, y + 1, z));
	}
	
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// fake grass block @(0, +1, 0) at render location
		IBlockAccess originalBA = renderer.blockAccess;
		renderer.blockAccess = new OffsetBlockAccess(world, x, y, z, 0, 1, 0);
		
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
