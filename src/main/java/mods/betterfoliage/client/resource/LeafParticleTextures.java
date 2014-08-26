package mods.betterfoliage.client.resource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import mods.betterfoliage.client.TextureMatcher;
import mods.betterfoliage.client.render.IconSet;
import mods.betterfoliage.client.resource.LeafTextureEnumerator.LeafTextureFoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
	
	protected void addAtlasTexture(TextureAtlasSprite icon) {
	    Integer textureColor = calculateTextureColor(icon);
	    if (textureColor != null) iconColors.put(icon, textureColor);
	    
	    String leafType = leafTypes.put(icon);
	    if (leafType != null && !iconSets.keySet().contains(leafType)) {
	        iconSets.put(leafType, new IconSet("betterfoliage", String.format("falling_leaf_%s_%%d", leafType)));
	    }
	}
	
	/** Calculate average color value (in HSB color space) for a texture.
     * @param icon texture
     */
	protected Integer calculateTextureColor(TextureAtlasSprite icon) {
		ResourceLocation locationNoDirs = new ResourceLocation(icon.getIconName());
		ResourceLocation locationWithDirs = new ResourceLocation(locationNoDirs.getResourceDomain(), String.format("textures/blocks/%s.png", locationNoDirs.getResourcePath()));
		try {
			BufferedImage image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(locationWithDirs).getInputStream());
			
			int numOpaque = 0;
			float sumHueX = 0.0f;
			float sumHueY = 0.0f;
			float sumSaturation = 0.0f;
			float sumBrightness = 0.0f;
			for (int x = 0; x < image.getWidth(); x++) for (int y = 0; y < image.getHeight(); y++) {
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xFF;
				float[] hsbVals = Color.RGBtoHSB((pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF, null);
				if (alpha == 255) {
					numOpaque++;
					sumHueX += Math.cos((hsbVals[0] - 0.5) * 2.0 * Math.PI);
					sumHueY += Math.sin((hsbVals[0] - 0.5) * 2.0 * Math.PI);
					sumSaturation += hsbVals[1];
					sumBrightness += hsbVals[2];
				}
			}
			
			// average hue as usual for circular values - transform average unit vector back to polar angle
			float avgHue = (float) (Math.atan2(sumHueY, sumHueX) / (2.0 * Math.PI) + 0.5);
			return Color.HSBtoRGB(avgHue, sumSaturation / numOpaque, sumBrightness / numOpaque);
		} catch (IOException e) {
		    return null;
		}
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		iconSets.clear();
		iconColors.clear();
		
		leafTypes.loadMappings(new ResourceLocation("betterfoliage", "leafTextureMappings.cfg"));
		IconSet defaultIcons = new IconSet("betterfoliage", "falling_leaf_default_%d");
		iconSets.put("default", defaultIcons);
		defaultIcons.registerIcons(event.map);
	}
	
	@SubscribeEvent
	public void handleRegisterTexture(LeafTextureFoundEvent event) {
		addAtlasTexture(event.icon);
	}
	
}
