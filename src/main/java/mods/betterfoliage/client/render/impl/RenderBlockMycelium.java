package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
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
public class RenderBlockMycelium extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet myceliumIcons = new IconSet("bettergrassandleaves", "better_mycel_%d");
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.myceliumEnabled) return false;
		if (block != Blocks.mycelium) return false;
		if (getCameraDistance(x, y, z) > Config.grassDistance) return false;
		if (!blockAccess.isAirBlock(x, y + 1, z)) return false;
		return true;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);
		
		int iconVariation = getSemiRandomFromPos(x, y, z, 0);
		IIcon renderIcon = myceliumIcons.get(iconVariation);
		
		if (renderIcon == null) return true;
		
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		double scale = Config.grassSize * 0.5;
		double halfHeight = 0.5 * (Config.grassHeightMin + pRand[heightVariation] * (Config.grassHeightMax - Config.grassHeightMin));
		
		// render mycelium
		ShadersModIntegration.startGrassQuads();
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
		Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0, z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[iconVariation], Config.grassHOffset, renderIcon, 0, false);
		
		return true;
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		myceliumIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d mycelium textures", myceliumIcons.numLoaded));
	}

}
