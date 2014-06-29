package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.FakeRenderBlockAOBase;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
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
public class RenderBlockBetterLilypad extends FakeRenderBlockAOBase implements IRenderBlockDecorator {

	public IIcon lilypadFlowers[] = new IIcon[2];
	public IIcon lilypadRoots[] = new IIcon[3];
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return Config.lilypadEnabled && block == Blocks.waterlily;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render grass block
		renderBlockLilyPad(block, x, y, z);
		
		int chanceVariation = getSemiRandomFromPos(x, y, z, 0);
		int iconVariation = getSemiRandomFromPos(x, y, z, 1);
		int offsetVariation = getSemiRandomFromPos(x, y, z, 2);
		
		Tessellator.instance.setBrightness(getBrightness(block, x, y, z));
		Tessellator.instance.setColorOpaque(255, 255, 255);
		renderCrossedSideQuads(new Double3(x + 0.5, y + 0.015, z + 0.5), ForgeDirection.DOWN,
							   0.2, 0.3,
							   null, 0.0,
							   lilypadRoots[iconVariation % 3], 2,
							   true);
		if (chanceVariation < Config.lilypadChance.value)
			renderCrossedSideQuads(new Double3(x + 0.5, y + 0.02, z + 0.5), ForgeDirection.UP,
					 			   0.2, 0.3, 
					 			   pRot[offsetVariation], Config.lilypadHOffset.value, 
					 			   lilypadFlowers[iconVariation % 2], 0,
					 			   true);
		
		return true;
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		for (int idx = 0; idx < 2; idx++) lilypadFlowers[idx] = event.map.registerIcon("bettergrassandleaves:better_lilypad_flower_" + Integer.toString(idx));
		for (int idx = 0; idx < 3; idx++) lilypadRoots[idx] = event.map.registerIcon("bettergrassandleaves:better_lilypad_roots_" + Integer.toString(idx));
	}
	
}
