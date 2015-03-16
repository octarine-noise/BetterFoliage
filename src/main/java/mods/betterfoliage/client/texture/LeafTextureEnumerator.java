package mods.betterfoliage.client.texture;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.integration.OptifineIntegration;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Enumerates all leaf textures at stitch time and emits an event for each.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class LeafTextureEnumerator implements IIconRegister {

	/**{@link Event} that is emitted for each texture belonging to a leaf block.
	 * @author octarine-noise
	 */
	@SideOnly(Side.CLIENT)
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
	
	/** Leaf blocks register their textures here.
	 *  @return the originally registered {@link IIcon} already in the atlas
	 */
	public IIcon registerIcon(String resourceLocation) {
		TextureAtlasSprite original = blockTextures.getTextureExtry(resourceLocation);
		MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, original));
		BetterFoliage.log.debug(String.format("Found leaf texture: %s", resourceLocation));
		
		Collection<IIcon> ctmIcons = OptifineIntegration.getAllCTMForIcon(original);
		if (!ctmIcons.isEmpty()) {
			BetterFoliage.log.info(String.format("Found %d CTM variants for texture %s", ctmIcons.size(), original.getIconName()));
			for (IIcon ctmIcon : ctmIcons) {
				MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, (TextureAtlasSprite) ctmIcon));
			}
		}
		return original;
	}

	/** Iterates through all leaf blocks in the registry and makes them register
	 *  their textures to "sniff out" all leaf textures.
	 * @param event
	 */
	@SubscribeEvent(priority=EventPriority.LOWEST)
	@SuppressWarnings("unchecked")
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = event.map;
		
		BetterFoliage.log.info("Reloading leaf textures");
		
		// register simple block textures
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while(iter.hasNext()) {
			Block block = iter.next();
			if (Config.leaves.matchesClass(block)) {
				BetterFoliage.log.debug(String.format("Inspecting leaf block: %s", block.getClass().getName()));
				block.registerBlockIcons(this);
				
				Collection<IIcon> ctmIcons = OptifineIntegration.getAllCTMForBlock(block);
				if (!ctmIcons.isEmpty()) {
					BetterFoliage.log.info(String.format("Found %d CTM texture variants for block: id=%d class=%s", ctmIcons.size(), Block.getIdFromBlock(block), block.getClass().getName()));
					for (IIcon ctmIcon : ctmIcons) {
						MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, (TextureAtlasSprite) ctmIcon));
					}
				}
			}
		}
		
		// enumerate all registered textures, find leaf textures among them
		try {
		    Map<String, TextureAtlasSprite> mapAtlas = CodeRefs.fMapRegisteredSprites.getInstanceField(blockTextures);
            
            Set<TextureAtlasSprite> foundLeafTextures = Sets.newHashSet();
            for (TextureAtlasSprite icon : mapAtlas.values())
                if (BetterFoliageClient.isLeafTexture(icon)) foundLeafTextures.add(icon);
            for (TextureAtlasSprite icon : foundLeafTextures) {
                BetterFoliage.log.debug(String.format("Found non-block-registered leaf texture: %s", icon.getIconName()));
                MinecraftForge.EVENT_BUS.post(new LeafTextureFoundEvent(blockTextures, icon));
            }
        } catch (UnableToAccessFieldException e) {
            BetterFoliage.log.warn("Failed to reflect texture atlas, textures may be missing");
        }
	}
	
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		if (event.map.getTextureType() != 0) return;
		blockTextures = null;
	}
	
}
