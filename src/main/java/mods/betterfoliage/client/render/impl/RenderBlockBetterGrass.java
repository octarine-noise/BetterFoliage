package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.ShadersModIntegration;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.util.Double3;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterGrass extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet grassIcons = new IconSet("bettergrassandleaves", "better_grass_long_%d");
	public IconSet snowGrassIcons = new IconSet("bettergrassandleaves", "better_grass_snowed_%d");
	public IconSet myceliumIcons = new IconSet("bettergrassandleaves", "better_mycel_%d");
	public IIcon grassGenIcon;
	public IIcon snowGrassGenIcon;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!BetterFoliage.config.grassEnabled) return false;
		if (!(BetterFoliageClient.grass.matchesID(block) || block == Blocks.mycelium)) return false;
		if (!blockAccess.isAirBlock(x, y + 1, z) && blockAccess.getBlock(x, y + 1, z) != Blocks.snow_layer) return false;
		return true;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// use original renderer for block breaking overlay
		if (renderer.hasOverrideBlockTexture()) {
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			return true;
		}
		
		// render grass block
		setPassCounters(1);
		setRenderBoundsFromBlock(block);
		if (block.getRenderType() == 0) {
			renderStandardBlock(block, x, y, z);
		} else {
			ISimpleBlockRenderingHandler handler = Utils.getRenderingHandler(block.getRenderType());
			handler.renderWorldBlock(world, x, y, z, block, block.getRenderType(), this);
		}
		
		int variation = getSemiRandomFromPos(x, y, z, 0);
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		boolean isSnowed = blockAccess.getBlock(x, y + 1, z) == Blocks.snow_layer;
		
		IIcon renderIcon = null;
		if (BetterFoliageClient.grass.matchesID(block)) {
			if (BetterFoliage.config.grassUseGenerated) {
				renderIcon = isSnowed ? snowGrassGenIcon : grassGenIcon;
			} else {
				renderIcon = isSnowed ? snowGrassIcons.get(variation) : grassIcons.get(variation);
			}
		} else if (block == Blocks.mycelium && !isSnowed) {
			renderIcon = myceliumIcons.get(variation);
		}
		if (renderIcon == null) return true;
		
		double scale = BetterFoliage.config.grassSize.value * 0.5;
		double halfHeight = 0.5 * (BetterFoliage.config.grassHeightMin.value + pRand[heightVariation] * (BetterFoliage.config.grassHeightMax.value - BetterFoliage.config.grassHeightMin.value));
		
		if (isSnowed) {
			aoYPXZNN.setGray(0.9f); aoYPXZNP.setGray(0.9f); aoYPXZPN.setGray(0.9f); aoYPXZPP.setGray(0.9f);
			Tessellator.instance.setColorOpaque(230, 230, 230);
		}
		
		// render short grass
		ShadersModIntegration.startGrassQuads();
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
		Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 + (isSnowed ? 0.0625 : 0.0), z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[variation], BetterFoliage.config.grassHOffset.value, renderIcon, 0, false);
		
		return true;
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		grassIcons.registerIcons(event.map);
		snowGrassIcons.registerIcons(event.map);
		myceliumIcons.registerIcons(event.map);
		grassGenIcon = event.map.registerIcon("bf_shortgrass:minecraft:tallgrass");
		snowGrassGenIcon = event.map.registerIcon("bf_shortgrass_snow:minecraft:tallgrass");
		BetterFoliage.log.info(String.format("Found %d short grass textures", grassIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d snowy grass textures", snowGrassIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d mycelium textures", myceliumIcons.numLoaded));
	}

}
