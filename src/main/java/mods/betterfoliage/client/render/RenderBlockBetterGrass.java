package mods.betterfoliage.client.render;

import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterGrass extends RenderBlockAOBase implements ISimpleBlockRenderingHandler {

	public IIcon grassIcons[] = new IIcon[2];
	
	public static int register() {
		int result = RenderingRegistry.getNextAvailableRenderId();
		RenderBlockBetterGrass renderGrass = new RenderBlockBetterGrass();
		RenderingRegistry.registerBlockHandler(result, renderGrass);
		MinecraftForge.EVENT_BUS.register(renderGrass);
		renderGrass.init();
		return result;
	}
	
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		renderStandardBlockAsItem(renderer, block, metadata, 1.0f);
	}

	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render grass block
		setRenderBoundsFromBlock(block);
		setPassCounters(1);
		boolean result = renderStandardBlock(block, x, y, z);
		
		if (y == 255 || !blockAccess.isAirBlock(x, y + 1, z)) return result;
		
		int variation = getSemiRandomFromPos(x, y, z, 0);
		int heightVariation = getSemiRandomFromPos(x, y, z, 1);
		double halfSize = Config.grassSize.value * 0.5;
		double halfHeight = 0.5 * (Config.grassHeightMin.value + pRand[heightVariation] * (Config.grassHeightMax.value - Config.grassHeightMin.value));
		Double3 drawCenter = new Double3(x + 0.5, y + 1.0 + halfHeight, z + 0.5).add(pRot[variation].scaleAxes(Config.grassHOffset.value, 0.0, Config.grassHOffset.value));
		Double3 horz1 = new Double3(halfSize, 0.0, halfSize);
		Double3 horz2 = new Double3(halfSize, 0.0, -halfSize);
		Double3 vert1 = new Double3(0.0, halfHeight, 0.0);
		IIcon grassIcon = grassIcons[variation % 2];
		
		if (Minecraft.isAmbientOcclusionEnabled()) {
			renderQuadWithShading(grassIcon, drawCenter, horz1, vert1, 0, 				aoYPXZPP, aoYPXZNN, aoYPXZNN, aoYPXZPP);
			renderQuadWithShading(grassIcon, drawCenter, horz1.inverse(), vert1, 0, 	aoYPXZNN, aoYPXZPP, aoYPXZPP, aoYPXZNN);
			renderQuadWithShading(grassIcon, drawCenter, horz2, vert1, 0, 				aoYPXZPN, aoYPXZNP, aoYPXZNP, aoYPXZPN);
			renderQuadWithShading(grassIcon, drawCenter, horz2.inverse(), vert1, 0,		aoYPXZNP, aoYPXZPN, aoYPXZPN, aoYPXZNP);
		} else {
			Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y + 1, z));
			renderQuad(grassIcon, drawCenter, horz1, vert1, 0);
			renderQuad(grassIcon, drawCenter, horz1.inverse(), vert1, 0);
			renderQuad(grassIcon, drawCenter, horz2, vert1, 0);
			renderQuad(grassIcon, drawCenter, horz2.inverse(), vert1, 0);
		}
		return result;
	}

	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
	
	public int getRenderId() {
		return 0;
	}

	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		for (int idx = 0; idx < 2; idx++) {
			grassIcons[idx] = event.map.registerIcon(String.format("betterfoliage:grass_%d", idx));
		}
	}
}
