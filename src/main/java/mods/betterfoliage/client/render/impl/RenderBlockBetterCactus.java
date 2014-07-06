package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.FakeRenderBlockAOBase;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
public class RenderBlockBetterCactus extends FakeRenderBlockAOBase implements IRenderBlockDecorator {

	public IIcon cactusRoundIcon;
	public IconSet cactusSideIcons = new IconSet("bettergrassandleaves", "better_cactus_arm_%d");
	
	public static ForgeDirection[] cactusDirections = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
	public static double cactusRadius = 0.4375;
	
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return BetterFoliage.config.cactusEnabled && block == Blocks.cactus;
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render cactus center
		setPassCounters(1);
		setRenderBoundsFromBlock(block);
		
		Double3 blockCenter = new Double3(x + 0.5, y + 0.5, z + 0.5);
		renderStandardBlock(block, x, y, z);
		Tessellator.instance.setBrightness(getBrightness(block,x, y, z));
		renderCactusCore(block.getBlockTextureFromSide(ForgeDirection.UP.ordinal()),
						 block.getBlockTextureFromSide(ForgeDirection.NORTH.ordinal()),
						 blockCenter, 0);
		
		// render side growth
		ForgeDirection drawDirection = cactusDirections[getSemiRandomFromPos(x, y, z, 0) % 4];
		int iconVariation = getSemiRandomFromPos(x, y, z, 1);
		Double3 drawBase = blockCenter.add(new Double3(drawDirection).scale(cactusRadius));
		
		Tessellator.instance.setBrightness(getBrightness(block, x, y, z));
		if (cactusSideIcons.hasIcons()) renderCrossedSideQuads(drawBase, drawDirection, 0.5, 0.5, pRot[iconVariation], 0.2, cactusSideIcons.get(iconVariation), 0, false);
		renderCrossedBlockQuadsSkew(blockCenter, 0.65,
									pRot[iconVariation].scaleAxes(0.1, 0.0, 0.1),
									pRot[(iconVariation + 1) & 63].scaleAxes(0.1, 0.0, 0.1),
									cactusRoundIcon, iconVariation, false, false);
		return true;
	}

	protected void renderCactusCore(IIcon topIcon, IIcon sideIcon, Double3 blockCenter, int sideUvRot) {
		if (Minecraft.isAmbientOcclusionEnabled()) {
			renderQuadWithShading(sideIcon, blockCenter.add(cactusRadius, 0.0, 0.0), new Double3(0.0, 0.0, -0.5), new Double3(0.0, 0.5, 0.0), sideUvRot, aoXPYZPN, aoXPYZPP, aoXPYZNP, aoXPYZNN);
			renderQuadWithShading(sideIcon, blockCenter.add(-cactusRadius, 0.0, 0.0), new Double3(0.0, 0.0, 0.5), new Double3(0.0, 0.5, 0.0), sideUvRot, aoXNYZPP, aoXNYZPN, aoXNYZNN, aoXNYZNP);
			renderQuadWithShading(sideIcon, blockCenter.add(0.0, 0.0, cactusRadius), new Double3(0.5, 0.0, 0.0), new Double3(0.0, 0.5, 0.0), sideUvRot, aoZPXYPP, aoZPXYNP, aoZPXYNN, aoZPXYPN);
			renderQuadWithShading(sideIcon, blockCenter.add(0.0, 0.0, -cactusRadius), new Double3(-0.5, 0.0, 0.0), new Double3(0.0, 0.5, 0.0), sideUvRot, aoZNXYNP, aoZNXYPP, aoZNXYPN, aoZNXYNN);
			renderQuadWithShading(topIcon, blockCenter.add(0.0, 0.5, 0.0), new Double3(-0.5, 0.0, 0.0), new Double3(0.0, 0.0, 0.5), 0, aoYPXZNP, aoYPXZPP, aoYPXZPN, aoYPXZNN);
		} else {
			renderQuad(sideIcon, blockCenter.add(cactusRadius, 0.0, 0.0), new Double3(0.0, 0.0, -0.5), new Double3(0.0, 0.5, 0.0), sideUvRot);
			renderQuad(sideIcon, blockCenter.add(-cactusRadius, 0.0, 0.0), new Double3(0.0, 0.0, 0.5), new Double3(0.0, 0.5, 0.0), sideUvRot);
			renderQuad(sideIcon, blockCenter.add(0.0, 0.0, cactusRadius), new Double3(0.5, 0.0, 0.0), new Double3(0.0, 0.5, 0.0), sideUvRot);
			renderQuad(sideIcon, blockCenter.add(0.0, 0.0, -cactusRadius), new Double3(-0.5, 0.0, 0.0), new Double3(0.0, 0.5, 0.0), sideUvRot);
			renderQuad(topIcon, blockCenter.add(0.0, 0.5, 0.0), new Double3(-0.5, 0.0, 0.0), new Double3(0.0, 0.0, 0.5), 0);
		}
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		cactusRoundIcon = event.map.registerIcon("bettergrassandleaves:better_cactus");
		cactusSideIcons.registerIcons(event.map);
		BetterFoliage.log.info(String.format("Found %d cactus arm textures", cactusSideIcons.numLoaded));
	}
}
