package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.integration.OptifineIntegration;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLeaves extends RenderBlockAOBase implements IRenderBlockDecorator {
	
    public IconSet snowedLeavesIcons = new IconSet("bettergrassandleaves", "better_leaves_snowed_%d");
    
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.leavesEnabled) return false;
		if (original > 0 && original < 42) return false;
		return Config.leaves.matchesID(block) && !isBlockSurrounded(blockAccess, x, y, z);
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);

		// find generated texture to render with, assume the
		// "true" texture of the block is the one on the north size
		TextureAtlasSprite blockLeafIcon = null;
		try {
			blockLeafIcon = (TextureAtlasSprite) block.getIcon(world, x, y, z, ForgeDirection.NORTH.ordinal());
			IIcon ctmIcon = OptifineIntegration.getConnectedTexture(blockAccess, block, x, y, z, ForgeDirection.NORTH.ordinal(), blockLeafIcon);
			if (ctmIcon != null && ctmIcon != blockLeafIcon && ctmIcon instanceof TextureAtlasSprite) {
				blockLeafIcon = (TextureAtlasSprite) ctmIcon;
			}
		} catch (ClassCastException e) {
		}
		
		if (blockLeafIcon == null) {
			BetterFoliage.log.debug(String.format("null leaf texture, x:%d, y:%d, z:%d, meta:%d, block:%s", x, y, z, blockAccess.getBlockMetadata(x, y, z), block.getClass().getName()));
			return true;
		}
		IIcon crossLeafIcon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(BetterFoliageClient.leafGenerator.domainName + ":" + blockLeafIcon.getIconName());
		if (crossLeafIcon == null) {
			return true;
		}
		
		int offsetVariation = getSemiRandomFromPos(x, y, z, 0);
		int uvVariation = getSemiRandomFromPos(x, y, z, 1);
		double halfSize = 0.5 * Config.leavesSize;
		boolean isAirTop = blockAccess.isAirBlock(x, y + 1, z);
		boolean isAirBottom = blockAccess.isAirBlock(x, y - 1, z);
		
		boolean isSnowTop = blockAccess.getBlock(x, y + 1, z).getMaterial() == Material.snow;
		
		Tessellator.instance.setBrightness(isAirTop ? getBrightness(block, x, y + 1, z) : (isAirBottom ? getBrightness(block, x, y - 1, z) : getBrightness(block, x, y, z)));
		Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
		
		Double3 blockCenter = new Double3(x + 0.5, y + 0.5, z + 0.5);
		Double3 offset1 = pRot[offsetVariation].scaleAxes(Config.leavesHOffset, Config.leavesVOffset, Config.leavesHOffset);
		Double3 offset2 = pRot[(offsetVariation + 1) & 63].scaleAxes(Config.leavesHOffset, Config.leavesVOffset, Config.leavesHOffset);
		
		if (Config.leavesSkew) {
			renderCrossedBlockQuadsSkew(blockCenter, halfSize, offset1, offset2, crossLeafIcon, uvVariation, isAirTop, isAirBottom);
			if (isSnowTop) {
			    // clear biome colors
                aoYPXZNN.setGray(0.9f); aoYPXZNP.setGray(0.9f); aoYPXZPN.setGray(0.9f); aoYPXZPP.setGray(0.9f);
                Tessellator.instance.setColorOpaque(230, 230, 230);
			    renderCrossedBlockQuadsSkew(blockCenter, halfSize, offset1, offset2, snowedLeavesIcons.get(uvVariation), 0, true, isAirBottom);
			}
		} else {
			renderCrossedBlockQuadsTranslate(blockCenter, halfSize, offset1, crossLeafIcon, uvVariation, isAirTop, isAirBottom);
			if (isSnowTop) {
			    // clear biome colors
                aoYPXZNN.setGray(0.9f); aoYPXZNP.setGray(0.9f); aoYPXZPN.setGray(0.9f); aoYPXZPP.setGray(0.9f);
                Tessellator.instance.setColorOpaque(230, 230, 230);
			    renderCrossedBlockQuadsTranslate(blockCenter, halfSize, offset1, snowedLeavesIcons.get(uvVariation), 0, true, isAirBottom);
			}
		}

		return true;
	}

	protected boolean isBlockSurrounded(IBlockAccess blockAccess, int x, int y, int z) {
		if (isBlockNonSurrounding(blockAccess.getBlock(x + 1, y, z))) return false;
		if (isBlockNonSurrounding(blockAccess.getBlock(x - 1, y, z))) return false;
		if (isBlockNonSurrounding(blockAccess.getBlock(x, y + 1, z))) return false;
		if (isBlockNonSurrounding(blockAccess.getBlock(x, y - 1, z))) return false;
		if (isBlockNonSurrounding(blockAccess.getBlock(x, y, z + 1))) return false;
		if (isBlockNonSurrounding(blockAccess.getBlock(x, y, z - 1))) return false;
		return true;
	}
	
	protected boolean isBlockNonSurrounding(Block block) {
		return block.getMaterial() == Material.air || block == Blocks.snow_layer;
	}
	
	@SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
	    if (event.map.getTextureType() != 0) return;
        
	    snowedLeavesIcons.registerIcons(event.map);
        BetterFoliage.log.info(String.format("Found %d snowed leaves textures", snowedLeavesIcons.numLoaded));
    }
}
