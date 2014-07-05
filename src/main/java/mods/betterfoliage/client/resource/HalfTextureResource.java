package mods.betterfoliage.client.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

/** {@link IResource} of PNG containing one half (top or bottom) of a given texture resource
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class HalfTextureResource implements IResource {

	/** Raw PNG data*/
	protected byte[] data = null;
	
	public HalfTextureResource(ResourceLocation resource, boolean bottom) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		try {
			// load full texture
			ResourceLocation origResource = new ResourceLocation(resource.getResourceDomain(), "textures/blocks/" + resource.getResourcePath());
			BufferedImage origImage = ImageIO.read(resourceManager.getResource(origResource).getInputStream());

			// draw half texture
			BufferedImage result = new BufferedImage(origImage.getWidth(), origImage.getHeight() / 2, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D graphics = result.createGraphics();
			graphics.drawImage(origImage, 0, bottom ? -origImage.getHeight() / 2 : 0, null);

			// create PNG image
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(result, "PNG", baos);
			data = baos.toByteArray();
		} catch (Exception e) {
			// stop log spam with GLSL installed
			if (e instanceof FileNotFoundException) return;
			BetterFoliage.log.info(String.format("Could not load texture: %s, exception: %s", resource.toString(), e.getClass().getSimpleName()));
		}
	}
	
	@Override
	public InputStream getInputStream() {
		return data != null ? new ByteArrayInputStream(data) : null;
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
