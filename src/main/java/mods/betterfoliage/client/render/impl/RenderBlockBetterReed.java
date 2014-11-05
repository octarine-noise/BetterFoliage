package mods.betterfoliage.client.render.impl;

import java.util.Random;

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
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterReed extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet reedBottomIcons = new IconSet("bf_reed_bottom", "bettergrassandleaves:better_reed_%d");
	public IconSet reedTopIcons = new IconSet("bf_reed_top", "bettergrassandleaves:better_reed_%d");
	public NoiseGeneratorSimplex noise;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.reedEnabled) return false;
		if (!(Config.dirt.matchesID(block))) return false;
		if (!Config.reedBiomeList.contains(blockAccess.getBiomeGenForCoords(x, z).biomeID)) return false;
		if (blockAccess.getBlock(x, y + 1, z).getMaterial() != Material.water) return false;
		if (!blockAccess.isAirBlock(x, y + 2, z)) return false;
		int terrainVariation = MathHelper.floor_double((noise.func_151605_a(x, z) + 1.0) * 32.0);
		return terrainVariation < Config.reedPopulation;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);
		
		int iconVariation = getSemiRandomFromPos(x, y, z, 0);
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		
		IIcon bottomIcon = reedBottomIcons.get(iconVariation);
		IIcon topIcon = reedTopIcons.get(iconVariation);
		if (bottomIcon == null || topIcon == null) return true;
		
		double quarterHeight = 0.25 * (Config.reedHeightMin + pRand[heightVariation] * (Config.reedHeightMax - Config.reedHeightMin));
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 2, z));
		Tessellator.instance.setColorOpaque(255, 255, 255);
		
		// render reeds
		if (Config.reedShaderWind) ShadersModIntegration.startGrassQuads();
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0, z + 0.5), ForgeDirection.UP, 0.5, quarterHeight, pRot[iconVariation], Config.reedHOffset, bottomIcon, 0, true);
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 + 2.0 * quarterHeight, z + 0.5), ForgeDirection.UP, 0.5, quarterHeight, pRot[iconVariation], Config.reedHOffset, topIcon, 0, true);
		
		return true;
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		reedBottomIcons.registerIcons(event.map);
		reedTopIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d reed textures", reedBottomIcons.numLoaded));
	}
	
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		noise = new NoiseGeneratorSimplex(new Random(event.world.getWorldInfo().getSeed()));
	}
	
}
