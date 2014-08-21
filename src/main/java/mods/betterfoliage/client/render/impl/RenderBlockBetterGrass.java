package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.ShadersModIntegration;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterGrass extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet grassIcons = new IconSet("bettergrassandleaves", "better_grass_long_%d");
	public IconSet snowGrassIcons = new IconSet("bettergrassandleaves", "better_grass_snowed_%d");
	public IIcon grassGenIcon;
	public IIcon snowGrassGenIcon;
	
	protected IIcon grassTopIcon;
	boolean isSnowTop;
	protected boolean connectXP, connectXN, connectZP, connectZN;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return Config.grass.matchesID(block);
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		
		// check for connected grass
		checkConnectedGrass(x, y, z);
		grassTopIcon = block.getIcon(blockAccess, x, y, z, ForgeDirection.UP.ordinal());
		
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);
		if (!Config.grassEnabled) return true;
		
		Material topMaterial = blockAccess.getBlock(x, y + 1, z).getMaterial();
		isSnowTop = (topMaterial == Material.snow || topMaterial == Material.craftedSnow); 
		boolean isAirTop = blockAccess.isAirBlock(x, y + 1, z);
		
		if (isSnowTop || isAirTop) {
			// render short grass
			int iconVariation = getSemiRandomFromPos(x, y, z, 0);
			int heightVariation = getSemiRandomFromPos(x, y, z, 1);
			
			double scale = Config.grassSize * 0.5;
			double halfHeight = 0.5 * (Config.grassHeightMin + pRand[heightVariation] * (Config.grassHeightMax - Config.grassHeightMin));
			
			IIcon shortGrassIcon = null;
			if (isSnowTop) {
				// clear biome colors
				aoYPXZNN.setGray(0.9f); aoYPXZNP.setGray(0.9f); aoYPXZPN.setGray(0.9f); aoYPXZPP.setGray(0.9f);
				Tessellator.instance.setColorOpaque(230, 230, 230);
				shortGrassIcon = Config.grassUseGenerated ? snowGrassGenIcon : snowGrassIcons.get(iconVariation);
			} else {
				Tessellator.instance.setColorOpaque_I(block.colorMultiplier(blockAccess, x, y, z));
				shortGrassIcon = Config.grassUseGenerated ? grassGenIcon : grassIcons.get(iconVariation);
			}
			if (shortGrassIcon == null) return true;
			
			ShadersModIntegration.startGrassQuads();
			Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
			renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 + (isSnowTop ? 0.0625 : 0.0), z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[iconVariation], Config.grassHOffset, shortGrassIcon, 0, false);
		}
		return true;
	}

	protected void checkConnectedGrass(int x, int y, int z) {
	    if (isSnowTop) {
	        connectXP = false;
	        connectXN = false;
	        connectZP = false;
	        connectZN = false;
	        return;
	    }
		Block blockBelow = blockAccess.getBlock(x, y - 1, z);
		if (Config.ctxGrassAggressiveEnabled && (Config.grass.matchesID(blockBelow) || Config.dirt.matchesID(blockBelow))) {
			connectXP = true;
			connectXN = true;
			connectZP = true;
			connectZN = true;
		} else if (Config.ctxGrassClassicEnabled) {
			connectXP = Config.grass.matchesID(blockAccess.getBlock(x + 1, y - 1, z));
			connectXN = Config.grass.matchesID(blockAccess.getBlock(x - 1, y - 1, z));
			connectZP = Config.grass.matchesID(blockAccess.getBlock(x, y - 1, z + 1));
			connectZN = Config.grass.matchesID(blockAccess.getBlock(x, y - 1, z - 1));
		} else {
			connectXP = false;
			connectXN = false;
			connectZP = false;
			connectZN = false;
		}
	}
	
	@Override
	public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
		super.renderFaceZNeg(block, x, y, z, connectZN ? grassTopIcon : icon);
	}

	@Override
	public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
		super.renderFaceZPos(block, x, y, z, connectZP ? grassTopIcon : icon);
	}

	@Override
	public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
		super.renderFaceXNeg(block, x, y, z, connectXN ? grassTopIcon : icon);
	}

	@Override
	public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
		super.renderFaceXPos(block, x, y, z, connectXP ? grassTopIcon : icon);
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		grassIcons.registerIcons(event.map);
		snowGrassIcons.registerIcons(event.map);
		grassGenIcon = event.map.registerIcon("bf_shortgrass:minecraft:tallgrass");
		snowGrassGenIcon = event.map.registerIcon("bf_shortgrass_snow:minecraft:tallgrass");
		BetterFoliage.log.info(String.format("Found %d short grass textures", grassIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d snowy grass textures", snowGrassIcons.numLoaded));
	}

}
