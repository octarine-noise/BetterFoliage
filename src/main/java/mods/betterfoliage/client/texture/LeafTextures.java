package mods.betterfoliage.client.texture;

import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.misc.BetterFoliageMetadataSection;
import mods.betterfoliage.client.misc.TextureMatcher;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.texture.LeafTextureEnumerator.LeafTextureFoundEvent;
import mods.betterfoliage.client.util.RenderUtils;
import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Holds rendering information about leaf blocks
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class LeafTextures {

    /** Rendering information for a leaf block 
     * @author octarine-noise
     */
    public static class LeafInfo {
        public TextureAtlasSprite roundLeafTexture;
        public int averageColor;
        public IconSet particleIcons;
        public boolean rotation = true;
    }

	/** Default color value */
	public static int defaultColor = 0x208040;

    /** Textures for leaf particles */
    public Map<String, IconSet> iconSets = Maps.newHashMap();
    
    /** Leaf type mappings */
    public TextureMatcher leafTypes = new TextureMatcher();
    
	/** Map of render information */
	public Map<IIcon, LeafInfo> leafInfoMap = Maps.newHashMap();
	
	/** Number of loaded particle texture sets */
	public int loadedSets;
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		loadedSets = 1;
		iconSets.clear();
		leafInfoMap.clear();
		leafTypes.clear();		
		leafTypes.loadMappings(new ResourceLocation("betterfoliage", "leafTextureMappings.cfg"));
	}
	
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        if (event.map.getTextureType() != 0) return;
        BetterFoliage.log.info(String.format("Loaded %d leaf particle sets", loadedSets));
    }
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void handleRegisterTexture(LeafTextureFoundEvent event) {
		LeafInfo leafInfo = new LeafInfo();
		
		// determine texture color
		Integer textureColor = RenderUtils.calculateTextureColor(event.icon);
	    leafInfo.averageColor = textureColor != null ? textureColor : defaultColor;
		
	    // get particle textures
	    String leafType = leafTypes.put(event.icon);
	    if (!iconSets.keySet().contains(leafType)) {
	        IconSet newSet = new IconSet("betterfoliage", String.format("falling_leaf_%s_%%d", leafType));
	        newSet.registerIcons(event.blockTextures);
	        iconSets.put(leafType, newSet);
	        loadedSets++;
	    }
	    leafInfo.particleIcons = iconSets.get(leafType);
	    
	    // get round leaf texture
	    leafInfo.roundLeafTexture = event.blockTextures.getTextureExtry(BetterFoliageClient.leafGenerator.getRoundLocationShort(event.icon.getIconName()).toString());
	    		
		// check metadata for rotation parameter
		IResource textureResource = ResourceUtils.getResource(BetterFoliageClient.leafGenerator.getRoundLocationFull(event.icon.getIconName()));
		if (textureResource != null && textureResource.hasMetadata()) {
			BetterFoliageMetadataSection metadata = (BetterFoliageMetadataSection) textureResource.getMetadata(BetterFoliage.METADATA_SECTION);
			if (metadata != null) {
				leafInfo.rotation = metadata.rotation;
			}
		}
		
		leafInfoMap.put(event.icon, leafInfo);
	}
	
}
