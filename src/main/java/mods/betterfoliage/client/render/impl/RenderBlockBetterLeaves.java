package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.util.Double3;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterLeaves extends RenderBlockAOBase implements IRenderBlockDecorator {
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!BetterFoliage.config.leavesEnabled) return false;
		if (original > 0 && original < 42) return false;
		return BetterFoliageClient.leaves.matchesID(block) && !isBlockSurrounded(blockAccess, x, y, z);
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render leaves center
		setPassCounters(1);
		setRenderBoundsFromBlock(block);
		if (block.getRenderType() == 0) {
			renderStandardBlock(block, x, y, z);
		} else {
			ISimpleBlockRenderingHandler handler = Utils.getRenderingHandler(block.getRenderType());
			handler.renderWorldBlock(world, x, y, z, block, block.getRenderType(), this);
		}

		// find generated texture to render with, assume the
		// "true" texture of the block is the one on the north size
		TextureAtlasSprite blockLeafIcon = (TextureAtlasSprite) block.getIcon(world, x, y, z, ForgeDirection.NORTH.ordinal());
		IIcon crossLeafIcon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(BetterFoliageClient.leafGenerator.domainName + ":" + blockLeafIcon.getIconName());
		if (crossLeafIcon == null) {
			return true;
		}
		
		int offsetVariation = getSemiRandomFromPos(x, y, z, 0);
		int uvVariation = getSemiRandomFromPos(x, y, z, 1);
		double halfSize = 0.5 * BetterFoliage.config.leavesSize.value;
		boolean isAirTop = y == 255 || blockAccess.isAirBlock(x, y + 1, z);
		boolean isAirBottom = y == 0 || blockAccess.isAirBlock(x, y - 1, z);
		
		Tessellator.instance.setBrightness(isAirTop ? getBrightness(block, x, y + 1, z) : (isAirBottom ? getBrightness(block, x, y - 1, z) : getBrightness(block, x, y, z)));
		Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
		
		if (BetterFoliage.config.leavesSkew) {
			renderCrossedBlockQuadsSkew(new Double3(x + 0.5, y + 0.5, z + 0.5), halfSize, 
										pRot[offsetVariation].scaleAxes(BetterFoliage.config.leavesHOffset.value, BetterFoliage.config.leavesVOffset.value, BetterFoliage.config.leavesHOffset.value),
										pRot[(offsetVariation + 1) & 63].scaleAxes(BetterFoliage.config.leavesHOffset.value, BetterFoliage.config.leavesVOffset.value, BetterFoliage.config.leavesHOffset.value),
										crossLeafIcon, uvVariation, isAirTop, isAirBottom);
		} else {
			renderCrossedBlockQuadsTranslate(new Double3(x + 0.5, y + 0.5, z + 0.5), halfSize, 
											 pRot[offsetVariation].scaleAxes(BetterFoliage.config.leavesHOffset.value, BetterFoliage.config.leavesVOffset.value, BetterFoliage.config.leavesHOffset.value),
											 crossLeafIcon, uvVariation, isAirTop, isAirBottom);
		}


		return true;
	}

	protected boolean isBlockSurrounded(IBlockAccess blockAccess, int x, int y, int z) {
		if (blockAccess.isAirBlock(x + 1, y, z)) return false;
		if (blockAccess.isAirBlock(x - 1, y, z)) return false;
		if (blockAccess.isAirBlock(x, y, z + 1)) return false;
		if (blockAccess.isAirBlock(x, y, z - 1)) return false;
		if (y == 255 || blockAccess.isAirBlock(x, y + 1, z)) return false;
		if (y == 0 || blockAccess.isAirBlock(x, y - 1, z)) return false;
		return true;
	}
	
}
