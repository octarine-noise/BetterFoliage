package mods.betterfoliage.client.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import mods.betterfoliage.client.integration.OptifineIntegration;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;


public class RenderUtils {

    /** Hide constructor */
    private RenderUtils() {}

    /** Retrieve a specific rendering handler from the registry
     * @param renderType render type of block
     * @return {@link ISimpleBlockRenderingHandler} if defined, null otherwise
     */
    @SuppressWarnings("unchecked")
    public static ISimpleBlockRenderingHandler getRenderingHandler(int renderType) {
    	try {
    		Field field = RenderingRegistry.class.getDeclaredField("INSTANCE");
    		field.setAccessible(true);
    		RenderingRegistry inst = (RenderingRegistry) field.get(null);
    		field = RenderingRegistry.class.getDeclaredField("blockRenderers");
    		field.setAccessible(true);
    		return ((Map<Integer, ISimpleBlockRenderingHandler>) field.get(inst)).get(renderType);
    	} catch (Exception e) {
    		return null;
    	}
    }

    /** Remove the lines from config GUI tooltips showing default values
     * @param tooltip tooltip lines
     */
    public static void stripTooltipDefaultText(List<String> tooltip) {
        boolean defaultRows = false;
        Iterator<String> iter = tooltip.iterator();
        while(iter.hasNext()) {
            if (iter.next().startsWith(EnumChatFormatting.AQUA.toString())) defaultRows = true;
            if (defaultRows) iter.remove();
        }
    }
    
    /** Retrieve actual texture used for rendering block face. Uses Optifine CTM if available.
     * @param blockAccess world instance
     * @param block
     * @param x
     * @param y
     * @param z
     * @param side
     * @return texture
     */
    public static IIcon getIcon(IBlockAccess blockAccess, Block block, int x, int y, int z, ForgeDirection side) {
    	IIcon base = block.getIcon(blockAccess, x, y, z, side.ordinal());
    	return OptifineIntegration.isPresent ? OptifineIntegration.getConnectedTexture(blockAccess, block, x, y, z, side.ordinal(), base) : base; 
    }
    
	/** Calculate average color value (in HSB color space) for a texture.
     * @param icon texture
	 * @return average color (in RGB space)
	 */
	public static Integer calculateTextureColor(TextureAtlasSprite icon) {
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
	
	public static int nextPowerOf2(int x) {
		return 1 << (x == 0 ? 0 : 32 - Integer.numberOfLeadingZeros(x - 1));
	}
}
