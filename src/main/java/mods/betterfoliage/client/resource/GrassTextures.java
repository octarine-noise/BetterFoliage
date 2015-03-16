package mods.betterfoliage.client.resource;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class GrassTextures implements IIconRegister {

	/** Texture atlas for block textures used in the current run */
	public TextureMap blockTextures;
	
	public Map<IIcon, Integer> avgColors = Maps.newHashMap();
	
	/** Leaf blocks register their textures here.
	 *  @return the originally registered {@link IIcon} already in the atlas
	 */
	public IIcon registerIcon(String resourceLocation) {
		TextureAtlasSprite original = blockTextures.getTextureExtry(resourceLocation);
		BetterFoliage.log.debug(String.format("Found grass texture: %s", resourceLocation));
		
		// get texture color
		int avgColor = RenderUtils.calculateTextureColor(original);
		float[] hsbVals = Color.RGBtoHSB((avgColor >> 16) & 0xFF, (avgColor >> 8) & 0xFF, avgColor & 0xFF, null);
		if (hsbVals[1] > 0.1) {
			// non-grey texture
			hsbVals[2] = 1.0f;
			avgColors.put(original, Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
		}
		return original;
	}
	
	/** Iterates through all grass blocks in the registry and makes them register
	 *  their textures to "sniff out" all leaf textures.
	 * @param event
	 */
	@SubscribeEvent(priority=EventPriority.LOWEST)
	@SuppressWarnings("unchecked")
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = event.map;
		avgColors = Maps.newHashMap();
		
		// register simple block textures
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while(iter.hasNext()) {
			Block block = iter.next();
			if (Config.grass.matchesClass(block)) {
				BetterFoliage.log.debug(String.format("Inspecting grass block: %s", block.getClass().getName()));
				block.registerBlockIcons(this);
			}
		}
	}
	
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = null;
	}
}
