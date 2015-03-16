package mods.betterfoliage.client.render.impl;

import java.util.Random;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
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
public class RenderBlockDirtWithAlgae extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet algaeIcons = new IconSet("bettergrassandleaves", "better_algae_%d");
	public NoiseGeneratorSimplex noise;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.algaeEnabled) return false;
		if (!Config.dirt.matchesID(block)) return false;
		if (!Config.algaeBiomeList.contains(blockAccess.getBiomeGenForCoords(x, z).biomeID)) return false;
		if (blockAccess.getBlock(x, y + 1, z).getMaterial() != Material.water) return false;
		if (blockAccess.getBlock(x, y + 2, z).getMaterial() != Material.water) return false;
		int terrainVariation = MathHelper.floor_double((noise.func_151605_a(x, z) + 1.0) * 32.0);
		return terrainVariation < Config.algaePopulation;
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
		
		// render dirt block
		setAOPassCounters(1);
		setRenderBoundsFromBlock(block);
		renderStandardBlock(block, x, y, z);
		
		int variation = getSemiRandomFromPos(x, y, z, 0);
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		
		IIcon renderIcon = algaeIcons.get(variation);
		if (renderIcon == null) return true;
		
		double scale = Config.algaeSize * 0.5;
		double halfHeight = 0.5 * (Config.algaeHeightMin + pRand[heightVariation] * (Config.algaeHeightMax - Config.algaeHeightMin));
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0 - 0.125 * halfHeight, z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[variation], Config.algaeHOffset, renderIcon, 0, false);
		
		return true;
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		algaeIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d algae textures", algaeIcons.numLoaded));
	}
	
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		noise = new NoiseGeneratorSimplex(new Random(event.world.getWorldInfo().getSeed() + 1));
	}
	
}
