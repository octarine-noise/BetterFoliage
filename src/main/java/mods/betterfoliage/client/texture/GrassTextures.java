package mods.betterfoliage.client.texture;

import java.awt.Color;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.util.RenderUtils;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Stores average texture color values for non-grey grass textures
 * @author octarine-noise
 */
public class GrassTextures implements IIconRegister {

	/** Texture atlas for block textures used in the current run */
	public TextureMap blockTextures;
	
	/** Map of average color values */
	public Map<IIcon, Integer> iconColors = Maps.newHashMap();
	
	public int getColor(IIcon icon, int defaultColor) {
		Integer result = iconColors.get(icon);
		return result == null ? defaultColor : result;
	}
	
	/** Grass blocks register their textures here.
	 *  @return the originally registered {@link IIcon} already in the atlas
	 */
	public IIcon registerIcon(String resourceLocation) {
		TextureAtlasSprite original = blockTextures.getTextureExtry(resourceLocation);
		if (original != null && !resourceLocation.startsWith("MISSING_ICON_BLOCK_")) {
			BetterFoliage.log.debug(String.format("Found grass texture: %s", resourceLocation));
		} else {
			BetterFoliage.log.warn(String.format("Invalid grass texture: %s", resourceLocation));
			return null;
		}
		
		// get texture color
		int avgColor = RenderUtils.calculateTextureColor(original);
		float[] hsbVals = Color.RGBtoHSB((avgColor >> 16) & 0xFF, (avgColor >> 8) & 0xFF, avgColor & 0xFF, null);
		if (hsbVals[1] > 0.1) {
			// non-grey texture
			hsbVals[2] = 1.0f;
			iconColors.put(original, Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
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
		iconColors = Maps.newHashMap();
		
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
