package mods.betterfoliage.client.render.impl;

import java.util.Random;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RenderBlockBetterCoral extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet coralCrustIcons = new IconSet("bettergrassandleaves", "better_crust_%d");
	public IconSet coralCrossIcons = new IconSet("bettergrassandleaves", "better_coral_%d");
	public NoiseGeneratorSimplex noise;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!BetterFoliage.config.coralEnabled) return false;
		if (block != Blocks.sand) return false;
		int terrainVariation = MathHelper.floor_double((noise.func_151605_a(x * 0.1, z * 0.1) + 1.0) * 32.0);
		return terrainVariation < BetterFoliage.config.coralPopulation.value;
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
		
		// render sand block
		setPassCounters(1);
		setRenderBoundsFromBlock(block);
		renderStandardBlock(block, x, y, z);
		
		Double3 blockCenter = new Double3(x + 0.5, y + 0.5, z + 0.5);
		double offset = pRand[getSemiRandomFromPos(x, y, z, 6)] * BetterFoliage.config.coralVOffset.value;
		double halfSize = BetterFoliage.config.coralSize.value * 0.5;
		double halfCrustSize = BetterFoliage.config.coralCrustSize.value * 0.5;
		
		Tessellator.instance.setBrightness(getBrightness(block, x, y, z));
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (blockAccess.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getMaterial() != Material.water) continue;
			if (blockAccess.isAirBlock(x + dir.offsetX, y + dir.offsetY + 1, z + dir.offsetZ)) continue;
				
			int variation = getSemiRandomFromPos(x, y, z, dir.ordinal());
			if (variation < BetterFoliage.config.coralChance.value) {
				IIcon crustIcon = coralCrustIcons.get(variation);
				IIcon coralIcon = coralCrossIcons.get(variation);
				if (crustIcon != null) renderCoralCrust(blockCenter, dir, offset, halfCrustSize, crustIcon, variation);
				if (coralIcon != null) renderCrossedSideQuads(blockCenter.add(new Double3(dir).scale(0.5)), dir, 
														      halfSize, halfSize, 
														      pRot[variation], BetterFoliage.config.coralHOffset.value,
														      coralIcon, 0, false);
			}
		}
		
		return true;
	}
	
	protected void renderCoralCrust(Double3 blockCenter, ForgeDirection dir, double offset, double scale, IIcon icon, int uvRot) {
		Double3 face1 = new Double3(faceDir1[dir.ordinal()]).scale(scale);
		Double3 face2 = new Double3(faceDir2[dir.ordinal()]).scale(scale);
		Double3 drawCenter = blockCenter.add(new Double3(dir).scale(0.5 + offset));
		if (Minecraft.isAmbientOcclusionEnabled()) {
			setShadingForFace(dir);
			renderQuadWithShading(icon, drawCenter, face1, face2, uvRot, faceAOPP, faceAONP, faceAONN, faceAOPN);
		} else {
			renderQuad(icon, drawCenter, face1, face2, uvRot);
		}
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		coralCrustIcons.registerIcons(event.map);
		coralCrossIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d coral crust textures", coralCrustIcons.numLoaded));
		BetterFoliage.log.info(String.format("Found %d coral textures", coralCrossIcons.numLoaded));
	}
	
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		noise = new NoiseGeneratorSimplex(new Random(event.world.getWorldInfo().getSeed() + 2));
	}
}
