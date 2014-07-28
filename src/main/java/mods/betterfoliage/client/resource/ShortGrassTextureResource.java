package mods.betterfoliage.client.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

public class ShortGrassTextureResource implements IResource {

	/** Raw PNG data*/
	public byte[] data = null;
	
	/** Resource to return if generation fails */
	public IResource fallbackResource;
	
	public ShortGrassTextureResource(ResourceLocation resource, boolean isSnowed, IResource fallbackResource) {
		this.fallbackResource = fallbackResource;
		boolean isSpecialTexture = resource.getResourcePath().toLowerCase().endsWith("_n.png") || resource.getResourcePath().toLowerCase().endsWith("_s.png");
		
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		try {
			// load full texture
			ResourceLocation origResource = new ResourceLocation(resource.getResourceDomain(), "textures/blocks/" + resource.getResourcePath());
			BufferedImage origImage = ImageIO.read(resourceManager.getResource(origResource).getInputStream());

			// draw bottom half of texture
			BufferedImage result = new BufferedImage(origImage.getWidth(), origImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D graphics = result.createGraphics();
			graphics.drawImage(origImage, 0, 3 * origImage.getHeight() / 8, null);

			// blend with white if snowed
			if (isSnowed && !isSpecialTexture) {
				for (int x = 0; x < result.getWidth(); x++) for (int y = 0; y < result.getHeight(); y++) {
					result.setRGB(x, y, blend(result.getRGB(x, y), 0xFFFFFF, 2, 3));
				}
			}
			
			// create PNG image
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(result, "PNG", baos);
			data = baos.toByteArray();
		} catch (Exception e) {
			// stop log spam with GLSL installed
			BetterFoliage.log.info(String.format("Could not load texture: %s, exception: %s", resource.toString(), e.getClass().getSimpleName()));
		}
	}

	protected int blend(int rgbOrig, int rgbBlend, int weightOrig, int weightBlend) {
		int r = ((rgbOrig & 0xFF) * weightOrig + (rgbBlend & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int g = (((rgbOrig >> 8) & 0xFF) * weightOrig + ((rgbBlend >> 8) & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int b = (((rgbOrig >> 16) & 0xFF) * weightOrig + ((rgbBlend >> 16) & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int a = (rgbOrig >> 24) & 0xFF;
		int result = (int) (a << 24 | b << 16 | g << 8 | r);
		return result;
	}
	
	@Override
	public InputStream getInputStream() {
		return data != null ? new ByteArrayInputStream(data) : fallbackResource.getInputStream();
	}

	@Override
	public boolean hasMetadata() {
		return false;
	}

	@Override
	public IMetadataSection getMetadata(String var1) {
		return null;
	}

}
