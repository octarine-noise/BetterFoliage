package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.FakeRenderBlockAOBase;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterLilypad extends FakeRenderBlockAOBase implements IRenderBlockDecorator {

	public IconSet lilypadFlowers = new IconSet("bettergrassandleaves", "better_lilypad_flower_%d");
	public IconSet lilypadRoots = new IconSet("bettergrassandleaves", "better_lilypad_roots_%d");
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return BetterFoliage.config.lilypadEnabled && block == Blocks.waterlily;
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
		if (lilypadRoots.hasIcons()) renderCrossedSideQuads(new Double3(x + 0.5, y + 0.015, z + 0.5), ForgeDirection.DOWN,
														   	0.2, 0.3,
														   	null, 0.0,
														   	lilypadRoots.get(iconVariation), 2,
														   	true);
		if (chanceVariation < BetterFoliage.config.lilypadChance.value && lilypadFlowers.hasIcons())
			renderCrossedSideQuads(new Double3(x + 0.5, y + 0.02, z + 0.5), ForgeDirection.UP,
					 			   0.2, 0.3, 
					 			   pRot[offsetVariation], BetterFoliage.config.lilypadHOffset.value, 
					 			   lilypadFlowers.get(iconVariation), 0,
					 			   true);
		
		return true;
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		lilypadFlowers.registerIcons(event.map);
		lilypadRoots.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d lilypad flower textures", lilypadFlowers.numLoaded));
		BetterFoliage.log.info(String.format("Found %d lilypad root textures", lilypadRoots.numLoaded));
	}
	
}
