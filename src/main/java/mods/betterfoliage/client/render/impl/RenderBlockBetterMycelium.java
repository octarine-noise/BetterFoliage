package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.ShadersModIntegration;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterMycelium extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet myceliumIcons = new IconSet("bettergrassandleaves", "better_mycel_%d");
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.myceliumEnabled) return false;
		if (block != Blocks.mycelium) return false;
		if (!blockAccess.isAirBlock(x, y + 1, z) && blockAccess.getBlock(x, y + 1, z) != Blocks.snow_layer) return false;
		return true;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);
		
		boolean isSnowed = blockAccess.getBlock(x, y + 1, z) == Blocks.snow_layer;
		int iconVariation = getSemiRandomFromPos(x, y, z, 0);
		IIcon renderIcon = myceliumIcons.get(iconVariation);
		
		if (isSnowed || renderIcon == null) return true;
		
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		double scale = Config.grassSize * 0.5;
		double halfHeight = 0.5 * (Config.grassHeightMin + pRand[heightVariation] * (Config.grassHeightMax - Config.grassHeightMin));
		
		if (isSnowed) {
			aoYPXZNN.setGray(0.9f); aoYPXZNP.setGray(0.9f); aoYPXZPN.setGray(0.9f); aoYPXZPP.setGray(0.9f);
			Tessellator.instance.setColorOpaque(230, 230, 230);
		}
		
		// render mycelium
		ShadersModIntegration.startGrassQuads();
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
		Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 + (isSnowed ? 0.0625 : 0.0), z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[iconVariation], Config.grassHOffset, renderIcon, 0, false);
		
		return true;
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		myceliumIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d mycelium textures", myceliumIcons.numLoaded));
	}

}
