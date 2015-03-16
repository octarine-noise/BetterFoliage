package mods.betterfoliage.client.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class ResourceUtils {

	/** Hide constructor */
	private ResourceUtils() {}
	
	/** Check for the existence of a {@link IResource}
	 * @param resourceLocation
	 * @return true if the resource exists
	 */
	public static boolean resourceExists(ResourceLocation resourceLocation) {
		try {
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			if (resource != null) return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	/** Copy a text file from a resource to the filesystem 
	 * @param resourceLocation resource location of text file
	 * @param target target file
	 * @throws IOException 
	 */
	public static void copyFromTextResource(ResourceLocation resourceLocation, File target) throws IOException {
		IResource defaults = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
		BufferedReader reader = new BufferedReader(new InputStreamReader(defaults.getInputStream(), Charsets.UTF_8));
		FileWriter writer = new FileWriter(target);
		
		String line = reader.readLine();
		while(line != null) {
			writer.write(line + System.lineSeparator());
			line = reader.readLine();
		}
		
		reader.close();
		writer.close();
	}
	
	public static Iterable<String> getLines(ResourceLocation resource) {
	    BufferedReader reader = null;
	    List<String> result = Lists.newArrayList();
        try {
            reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream(), Charsets.UTF_8));
            String line = reader.readLine();
            while(line != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) result.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            BetterFoliage.log.warn(String.format("Error reading resource %s", resource.toString()));
            return Lists.newArrayList();
        }
        return result;
	}
	
	public static Iterable<ModelBlock> iterateParents(final Map<ResourceLocation, ModelBlock> models, final ResourceLocation startModel) {
	    return new Iterable<ModelBlock>() {
            @Override
            public Iterator<ModelBlock> iterator() {
                return new Iterator<ModelBlock>() {
                    protected ModelBlock currentModel = models.get(startModel);
                    
                    @Override
                    public boolean hasNext() {
                        return currentModel != null;
                    }

                    @Override
                    public ModelBlock next() {
                        ModelBlock result = currentModel;
                        currentModel = models.get(currentModel.getParentLocation());
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
	}
	
	   /** Calculate average color value (in HSB color space) for a texture.
     * @param icon texture
     */
    public static Color4 calculateTextureColor(TextureAtlasSprite icon) {
        ResourceLocation locationNoDirs = new ResourceLocation(icon.getIconName());
        ResourceLocation locationWithDirs = new ResourceLocation(locationNoDirs.getResourceDomain(), String.format("textures/%s.png", locationNoDirs.getResourcePath()));
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
            return Color4.fromARGB(Color.HSBtoRGB(avgHue, sumSaturation / numOpaque, sumBrightness / numOpaque));
        } catch (IOException e) {
            return null;
        }
    }
}
