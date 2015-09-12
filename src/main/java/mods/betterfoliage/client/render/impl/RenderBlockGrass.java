package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.integration.TerraFirmaCraftIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.client.util.RenderUtils;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
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
public class RenderBlockGrass extends RenderBlockAOBase implements IRenderBlockDecorator {

    public enum RenderMode { DEFAULT, TFC };
    
	public IconSet grassIcons = new IconSet("bettergrassandleaves", "better_grass_long_%d");
	public IconSet snowGrassIcons = new IconSet("bettergrassandleaves", "better_grass_snowed_%d");
	public IconSet hangingGrassIcons = new IconSet("bettergrassandleaves", "better_grass_side_%d");
	public IIcon grassGenIcon;
	public IIcon snowGrassGenIcon;
	
	protected RenderMode currentMode;
	protected IIcon grassTopIcon;
	protected boolean isSnowTop;
	protected int blockColor;
	protected boolean connectXP, connectXN, connectZP, connectZN;
	
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return Config.grass.matchesID(block);
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		
		// check for special case rendering
		currentMode = RenderMode.DEFAULT;
		if (TerraFirmaCraftIntegration.isTFCBlock(block)) currentMode = RenderMode.TFC;
		
		// check for connected grass
		Material topMaterial = blockAccess.getBlock(x, y + 1, z).getMaterial();
		isSnowTop = (topMaterial == Material.snow || topMaterial == Material.craftedSnow); 
		checkConnectedGrass(x, y, z);
		
		// set colors and textures
		grassTopIcon = RenderUtils.getIcon(blockAccess, block, x, y, z, ForgeDirection.UP);
		blockColor = block.colorMultiplier(blockAccess, x, y, z);
		Integer avgColor = BetterFoliageClient.grassTextures.iconColors.get(grassTopIcon);
		boolean useTextureColor = (avgColor != null);
		
		if (renderWorldBlockBase(2, world, x, y, z, block, modelId, renderer)) return true;
		
		boolean isAirTop = blockAccess.isAirBlock(x, y + 1, z);
		int distance = getCameraDistance(x, y, z);
		
		if (Config.hangingGrassEnabled && distance <= Config.hangingGrassDistance) {
			// render hanging grass
			Double3 blockCenter = new Double3(x + 0.5, y + 0.5, z + 0.5);
			int iconVariation = getSemiRandomFromPos(x, y, z, 2);
			
			Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
			if (isSnowTop)
				Tessellator.instance.setColorOpaque(230, 230, 230);
			else
				Tessellator.instance.setColorOpaque_I(useTextureColor ? avgColor : blockColor);
			
			if (!blockAccess.getBlock(x + 1, y, z).isOpaqueCube()) renderHangingGrass(blockCenter, ForgeDirection.EAST, Config.hangingGrassSize, Config.hangingGrassSeparation, hangingGrassIcons.get(iconVariation));
			if (!blockAccess.getBlock(x - 1, y, z).isOpaqueCube()) renderHangingGrass(blockCenter, ForgeDirection.WEST, Config.hangingGrassSize, Config.hangingGrassSeparation, hangingGrassIcons.get(iconVariation));
			if (!blockAccess.getBlock(x, y, z + 1).isOpaqueCube()) renderHangingGrass(blockCenter, ForgeDirection.SOUTH, Config.hangingGrassSize, Config.hangingGrassSeparation, hangingGrassIcons.get(iconVariation));
			if (!blockAccess.getBlock(x, y, z - 1).isOpaqueCube()) renderHangingGrass(blockCenter, ForgeDirection.NORTH, Config.hangingGrassSize, Config.hangingGrassSeparation, hangingGrassIcons.get(iconVariation));
		}
		
		if (Config.grassEnabled && distance <= Config.grassDistance && (isAirTop || (isSnowTop && Config.grassSnowEnabled))) {
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
				Tessellator.instance.setColorOpaque_I(useTextureColor ? avgColor : blockColor);
				if (useTextureColor) {
					aoYPXZNN.setColor(avgColor); aoYPXZNP.setColor(avgColor); aoYPXZPN.setColor(avgColor); aoYPXZPP.setColor(avgColor);
				}
				shortGrassIcon = Config.grassUseGenerated ? grassGenIcon : grassIcons.get(iconVariation);
			}
			if (shortGrassIcon == null) return true;
			
			if (Config.grassShaderWind) ShadersModIntegration.startGrassQuads();
			Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
			renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 + (isSnowTop ? 0.0625 : 0.0), z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[iconVariation], Config.grassHOffset, shortGrassIcon, 0, false);
		}
		
		return true;
	}

	protected void renderHangingGrass(Double3 blockCenter, ForgeDirection face, double length, double separation, IIcon icon) {
		Double3 edgeCenter = blockCenter.add(new Double3(face).scale(0.5)).add(0.0, 0.5, 0.0);
		Double3 edge = new Double3(faceDir1[face.ordinal()]).scale(0.5);
		Double3 extrude = new Double3(face).scale(separation).add(0.0, -length, 0.0);
		
		ShadingValues shP = null;
		ShadingValues shN = null;
		if (Minecraft.isAmbientOcclusionEnabled() && !noShading) {
			shP = getAoLookup(ForgeDirection.UP, face, faceDir1[face.ordinal()]);
			shN = getAoLookup(ForgeDirection.UP, face, faceDir1[face.getOpposite().ordinal()]);
		}
		
		Double3 vert1 = edgeCenter.add(edge);
		Double3 vert2 = edgeCenter.sub(edge);
		renderQuad(icon, vert1, vert2, vert2.add(extrude), vert1.add(extrude), uValues, vValues, shP, shN, shN, shP);
//		renderQuadWithShading(icon, edgeCenter.add(extrude.scale(0.5)), edge.inverse(), extrude.scale(0.5), 2, faceAOPP, faceAONP, faceAONP, faceAOPP);
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
    public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
	    if (currentMode == RenderMode.TFC && Config.grass.matchesID(block)) grassTopIcon = icon;
        super.renderFaceYPos(block, x, y, z, icon);
    }

    @Override
	public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
	    if (shouldSkipPass(block, connectZN, drawPass)) return;
	    if (connectZN) setBiomeColors();
		super.renderFaceZNeg(block, x, y, z, getDrawTexture(block, icon, grassTopIcon, connectZN, drawPass));
	}

	@Override
	public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
	    if (shouldSkipPass(block, connectZP, drawPass)) return;
	    if (connectZP) setBiomeColors();
	    super.renderFaceZPos(block, x, y, z, getDrawTexture(block, icon, grassTopIcon, connectZP, drawPass));
	}

	@Override
	public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
        if (shouldSkipPass(block, connectXN, drawPass)) return;
        if (connectXN) setBiomeColors();
        super.renderFaceXNeg(block, x, y, z, getDrawTexture(block, icon, grassTopIcon, connectXN, drawPass));
	}

	@Override
	public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
        if (shouldSkipPass(block, connectXP, drawPass)) return;
        if (connectXP) setBiomeColors();
        super.renderFaceXPos(block, x, y, z, getDrawTexture(block, icon, grassTopIcon, connectXP, drawPass));
	}

	protected boolean shouldSkipPass(Block block, boolean connected, int pass) {
	    return currentMode != RenderMode.TFC && connected && pass > 1;
	}
	
	protected void setBiomeColors() {
	    if (fancyGrass) return;
	    Tessellator.instance.setColorOpaque_I(blockColor);
        if (enableAO) setAOColors(blockColor);
	}
	
	protected IIcon getDrawTexture(Block block, IIcon original, IIcon grassTop, boolean connected, int pass) {
	    if (currentMode == RenderMode.TFC) {
	        return connected && Config.grass.matchesID(block) ? grassTop : original;
	    } else {
	        return connected ? grassTop : original;
	    }
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		grassIcons.registerIcons(event.map);
		snowGrassIcons.registerIcons(event.map);
		hangingGrassIcons.registerIcons(event.map);
		grassGenIcon = event.map.registerIcon("bf_shortgrass:minecraft:tallgrass");
		snowGrassGenIcon = event.map.registerIcon("bf_shortgrass_snow:minecraft:tallgrass");
		BetterFoliage.log.info(String.format("Found %d short grass textures", grassIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d snowy grass textures", snowGrassIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d hanging grass textures", hangingGrassIcons.numLoaded));
	}

}
