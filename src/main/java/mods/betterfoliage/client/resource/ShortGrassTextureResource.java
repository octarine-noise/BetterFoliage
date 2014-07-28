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
	
	public ShortGrassTextureResource(ResourceLocation resource, IResource fallbackResource) {
		this.fallbackResource = fallbackResource;
		
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		try {
			// load full texture
			ResourceLocation origResource = new ResourceLocation(resource.getResourceDomain(), "textures/blocks/" + resource.getResourcePath());
			BufferedImage origImage = ImageIO.read(resourceManager.getResource(origResource).getInputStream());

			// draw bottom half of texture
			BufferedImage result = new BufferedImage(origImage.getWidth(), origImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D graphics = result.createGraphics();
			graphics.drawImage(origImage, 0, 3 * origImage.getHeight() / 8, null);

			// create PNG image
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(result, "PNG", baos);
			data = baos.toByteArray();
		} catch (Exception e) {
			// stop log spam with GLSL installed
			BetterFoliage.log.info(String.format("Could not load texture: %s, exception: %s", resource.toString(), e.getClass().getSimpleName()));
		}
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
