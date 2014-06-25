package mods.betterfoliage.client.render;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import mods.betterfoliage.common.util.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterLeaves extends RenderBlockAOBase implements ISimpleBlockRenderingHandler {
	
	public static int register() {
		int result = RenderingRegistry.getNextAvailableRenderId();
		RenderBlockBetterLeaves renderLeaves = new RenderBlockBetterLeaves();
		RenderingRegistry.registerBlockHandler(result, renderLeaves);
		renderLeaves.init();
		return result;
	}
	
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		renderStandardBlockAsItem(renderer, block, metadata, 1.0f);
	}

	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// store world for later use
		blockAccess = world;
		
		// render leaves center
		setPassCounters(1);
		int origRenderType = block.getRenderType();
		boolean result;
		
		setRenderBoundsFromBlock(block);
		if (origRenderType == 0) {
			result = renderStandardBlock(block, x, y, z);
		} else {
			ISimpleBlockRenderingHandler handler = ReflectionUtil.getRenderingHandler(origRenderType);
			result = handler.renderWorldBlock(world, x, y, z, block, origRenderType, this);
		}
		
		if (isBlockSurrounded(x, y, z)) return result;

		// find generated texture to render with, assume the
		// "true" texture of the block is the one on the north size
		TextureAtlasSprite blockLeafIcon = (TextureAtlasSprite) block.getIcon(world, x, y, z, ForgeDirection.NORTH.ordinal());
		IIcon crossLeafIcon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(BetterFoliageClient.leafGenerator.domainName + ":" + blockLeafIcon.getIconName());
		if (crossLeafIcon == null) {
			return result;
		}
		
		int variation = getSemiRandomFromPos(x, y, z, 0);
		double halfSize = 0.5 * Config.leavesSize.value;
		boolean isAirTop = y == 255 || blockAccess.isAirBlock(x, y + 1, z);
		boolean isAirBottom = y == 0 || blockAccess.isAirBlock(x, y - 1, z);
		Double3 drawCenter = new Double3(x + 0.5, y + 0.5, z + 0.5);
		Double3 horz1 = new Double3(halfSize, 0.0, halfSize).add(pRot[variation].scaleAxes(Config.leavesHOffset.value, Config.leavesVOffset.value, Config.leavesHOffset.value));
		Double3 horz2 = new Double3(halfSize, 0.0, -halfSize).add(pRot[(variation + 1) & 63].scaleAxes(Config.leavesHOffset.value, Config.leavesVOffset.value, Config.leavesHOffset.value));
		Double3 vert1 = new Double3(0.0, halfSize * 1.41, 0.0);
		
		if (Minecraft.isAmbientOcclusionEnabled()) {
			renderQuadWithShading(crossLeafIcon, drawCenter, horz1, vert1, variation,
					isAirTop ? aoYPXZPP : aoZPXYPP, isAirTop ? aoYPXZNN : aoXNYZPN, isAirBottom ? aoYNXZNN : aoXNYZNN, isAirBottom ? aoYNXZPP : aoZPXYPN);
			renderQuadWithShading(crossLeafIcon, drawCenter, horz1.inverse(), vert1, variation,
					isAirTop ? aoYPXZNN : aoZNXYNP, isAirTop ? aoYPXZPP : aoXPYZPP, isAirBottom ? aoYNXZPP : aoXPYZNP, isAirBottom ? aoYNXZNN : aoZNXYNN);
			renderQuadWithShading(crossLeafIcon, drawCenter, horz2, vert1, variation,
					isAirTop ? aoYPXZPN : aoXPYZPN, isAirTop ? aoYPXZNP : aoZPXYNP, isAirBottom ? aoYNXZNP : aoZPXYNN, isAirBottom ? aoYNXZPN : aoXPYZNN);
			renderQuadWithShading(crossLeafIcon, drawCenter, horz2.inverse(), vert1, variation,
					isAirTop ? aoYPXZNP : aoXNYZPP, isAirTop ? aoYPXZPN : aoZNXYPP, isAirBottom ? aoYNXZPN : aoZNXYPN, isAirBottom ? aoYNXZNP : aoXNYZNP);
		} else {
			if (isAirTop) Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y + 1, z));
			else if (isAirBottom) Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y - 1, z));
			else Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(blockAccess, x, y, z));
			
			renderQuad(crossLeafIcon, drawCenter, horz1, vert1, variation);
			renderQuad(crossLeafIcon, drawCenter, horz1.inverse(), vert1, variation);
			renderQuad(crossLeafIcon, drawCenter, horz2, vert1, variation);
			renderQuad(crossLeafIcon, drawCenter, horz2.inverse(), vert1, variation);
		}
		return result;
	}

	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
	
	public int getRenderId() {
		return 0;
	}
	
	protected boolean isBlockSurrounded(int x, int y, int z) {
		if (blockAccess.isAirBlock(x + 1, y, z)) return false;
		if (blockAccess.isAirBlock(x - 1, y, z)) return false;
		if (blockAccess.isAirBlock(x, y, z + 1)) return false;
		if (blockAccess.isAirBlock(x, y, z - 1)) return false;
		if (y == 255 || blockAccess.isAirBlock(x, y + 1, z)) return false;
		if (y == 0 || blockAccess.isAirBlock(x, y - 1, z)) return false;
		return true;
	}

}
