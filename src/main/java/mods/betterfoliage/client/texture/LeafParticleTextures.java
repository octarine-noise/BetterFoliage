package mods.betterfoliage.client.texture;

import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.misc.TextureMatcher;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.texture.LeafTextureEnumerator.LeafTextureFoundEvent;
import mods.betterfoliage.client.util.RenderUtils;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Holds the textures for the falling leaf particles, and stores average texture color values for leaf textures
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class LeafParticleTextures {

    /** Icons for leaf particles */
    public Map<String, IconSet> iconSets = Maps.newHashMap();
    
    /** Leaf type mappings */
    public TextureMatcher leafTypes = new TextureMatcher();
    
	/** Map of average color values */
	public Map<IIcon, Integer> iconColors = Maps.newHashMap();
	
	/** Default color value */
	public int defaultColor = 0x208040;
	
	public int loadedSets;
	
	public LeafParticleTextures(int defaultColor) {
		this.defaultColor = defaultColor;
	}
	
	public IconSet getIconSet(IIcon icon) {
	    String leafType = leafTypes.get(icon);
	    if (leafType == null) leafType = "default";
	    IconSet result = iconSets.get(leafType);
	    return result.hasIcons() ? result : iconSets.get("default");
	}
	
	public int getColor(IIcon icon) {
		Integer result = iconColors.get(icon);
		return result == null ? defaultColor : result;
	}
	
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		loadedSets = 1;
		iconSets.clear();
		iconColors.clear();
		
		leafTypes.loadMappings(new ResourceLocation("betterfoliage", "leafTextureMappings.cfg"));
		IconSet defaultIcons = new IconSet("betterfoliage", "falling_leaf_default_%d");
		iconSets.put("default", defaultIcons);
		defaultIcons.registerIcons(event.map);
	}
	
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        if (event.map.getTextureType() == 0) BetterFoliage.log.info(String.format("Loaded %d leaf particle sets", loadedSets));
    }
	
	@SubscribeEvent
	public void handleRegisterTexture(LeafTextureFoundEvent event) {
	    Integer textureColor = RenderUtils.calculateTextureColor(event.icon);
	    if (textureColor != null) iconColors.put(event.icon, textureColor);
    
	    String leafType = leafTypes.put(event.icon);
	    if (leafType != null && !iconSets.keySet().contains(leafType)) {
	        IconSet newSet = new IconSet("betterfoliage", String.format("falling_leaf_%s_%%d", leafType));
	        newSet.registerIcons(event.blockTextures);
	        iconSets.put(leafType, newSet);
	        loadedSets++;
	    }
	}
	
}
