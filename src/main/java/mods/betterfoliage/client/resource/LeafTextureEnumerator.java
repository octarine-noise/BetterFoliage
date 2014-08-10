package mods.betterfoliage.client.resource;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.util.Utils;
import mods.betterfoliage.loader.DeobfHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LeafTextureEnumerator implements IIconRegister {

	public static class LeafTextureFoundEvent extends Event {
		public TextureMap blockTextures;
		public TextureAtlasSprite icon;
		private LeafTextureFoundEvent(TextureMap blockTextures, TextureAtlasSprite icon) {
			super();
			this.blockTextures = blockTextures;
			this.icon = icon;
		}
	}
	
	/** Texture atlas for block textures used in the current run */
	public TextureMap blockTextures;
	
	/** Leaf blocks register their textures here. An extra texture will be registered in the atlas
	 *  for each, with the resource domain of this generator.
	 *  @return the originally registered {@link IIcon} already in the atlas
	 */
	public IIcon registerIcon(String resourceLocation) {
		TextureAtlasSprite original = blockTextures.getTextureExtry(resourceLocation);
		MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, original));
		BetterFoliage.log.debug(String.format("Found leaf texture: %s", resourceLocation));
		return original;
	}

	/** Iterates through all leaf blocks in the registry and makes them register
	 *  their textures to "sniff out" all leaf textures.
	 * @param event
	 */
	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = event.map;
		
		BetterFoliage.log.info("Reloading leaf textures");
		
		// register simple block textures
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while(iter.hasNext()) {
			Block block = iter.next();
			if (BetterFoliageClient.leaves.matchesClass(block)) {
				BetterFoliage.log.debug(String.format("Inspecting leaf block: %s", block.getClass().getName()));
				block.registerBlockIcons(this);
			}
		}
		
		// enumerate all registered textures, find leaf textures among them
		Map<String, TextureAtlasSprite> mapAtlas = null;
		mapAtlas = Utils.getField(blockTextures, DeobfHelper.transformElementSearge("mapRegisteredSprites"), Map.class);
		if (mapAtlas == null) mapAtlas = Utils.getField(blockTextures, "mapRegisteredSprites", Map.class);
		if (mapAtlas == null) {
			BetterFoliage.log.warn("Failed to reflect texture atlas, textures may be missing");
		} else {
			Set<TextureAtlasSprite> foundLeafTextures = Sets.newHashSet();
			for (TextureAtlasSprite icon : mapAtlas.values())
				if (BetterFoliageClient.isLeafTexture(icon)) foundLeafTextures.add(icon);
			for (TextureAtlasSprite icon : foundLeafTextures) {
				BetterFoliage.log.debug(String.format("Found non-block-registered leaf texture: %s", icon.getIconName()));
				MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, icon));
			}
		}
	}
	
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = null;
	}
	
}
