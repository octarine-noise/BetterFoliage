package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.RenderBlockAOBase;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
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
public class RenderBlockBetterGrass extends RenderBlockAOBase implements IRenderBlockDecorator {

	public IIcon grassIcons[] = new IIcon[5];
	public IIcon myceliumIcons[] = new IIcon[4];
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		if (!Config.grassEnabled) return false;
		if (!((block instanceof BlockGrass || block == Blocks.mycelium))) return false;
		if (y == 255 || !blockAccess.isAirBlock(x, y + 1, z)) return false;
		return true;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render grass block
		setPassCounters(1);
		setRenderBoundsFromBlock(block);
		renderStandardBlock(block, x, y, z);
		
		int variation = getSemiRandomFromPos(x, y, z, 0);
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		
		IIcon renderIcon = (block == Blocks.mycelium) ? myceliumIcons[variation % 4] : grassIcons[variation % 5];
		
		double scale = Config.grassSize.value * 0.5;
		double halfHeight = 0.5 * (Config.grassHeightMin.value + pRand[heightVariation] * (Config.grassHeightMax.value - Config.grassHeightMin.value));
		Tessellator.instance.setBrightness(getBrightness(block, x, y + 1, z));
		renderCrossedSideQuads(new Double3(x + 0.5, y + 1.0, z + 0.5), ForgeDirection.UP, scale, halfHeight, pRot[variation], Config.grassHOffset.value, renderIcon, 0, false);
		
		return true;
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		for (int idx = 0; idx < 5; idx++) {
			grassIcons[idx] = event.map.registerIcon(String.format("bettergrassandleaves:better_grass_long_%d", idx));
		}
		for (int idx = 0; idx < 4; idx++) {
			myceliumIcons[idx] = event.map.registerIcon(String.format("bettergrassandleaves:better_mycel_%d", idx));
		}
	}
}
