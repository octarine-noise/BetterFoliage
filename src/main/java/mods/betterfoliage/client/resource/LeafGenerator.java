package mods.betterfoliage.client.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.ShadersModIntegration;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

public class LeafGenerator extends BlockTextureGenerator {

	/** Resource domain name of pre-drawn textures */
	public String nonGeneratedDomain = "betterfoliage";
	
	/** Number of textures generated in the current run */
	public int generatedCounter = 0;
	
	/** Number of pre-drawn textures found in the current run */
	public int drawnCounter = 0;
	
	/** Name of the default alpha mask to use */
	public static String defaultMask = "rough";
	
	public LeafGenerator(String domainName, ResourceLocation missingResource) {
		super(domainName, missingResource);
	}

	@Override
	public IResource getResource(ResourceLocation resourceLocation) throws IOException {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		ResourceLocation originalNoDirs = unwrapResource(resourceLocation);
		ResourceLocation originalWithDirs = new ResourceLocation(originalNoDirs.getResourceDomain(), "textures/blocks/" + originalNoDirs.getResourcePath());
		
		// check for provided texture
		ResourceLocation handDrawnLocation = new ResourceLocation(nonGeneratedDomain, String.format("textures/blocks/%s/%s", originalNoDirs.getResourceDomain(), originalNoDirs.getResourcePath())); 
		if (Utils.resourceExists(handDrawnLocation)) {
			drawnCounter++;
			return resourceManager.getResource(handDrawnLocation);
		}
		
		// generate our own
		if (!Utils.resourceExists(originalWithDirs)) return getMissingResource();
		
		// load normal leaf texture
		BufferedImage origImage = ImageIO.read(resourceManager.getResource(originalWithDirs).getInputStream());
		if (origImage.getWidth() != origImage.getHeight()) return getMissingResource();
		int size = origImage.getWidth();

		// tile leaf texture 2x2
		BufferedImage overlayIcon = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = overlayIcon.createGraphics();
		graphics.drawImage(origImage, 0, 0, null);
		graphics.drawImage(origImage, 0, size, null);
		graphics.drawImage(origImage, size, 0, null);
		graphics.drawImage(origImage, size, size, null);
		
		// overlay mask alpha on texture
		if (!ShadersModIntegration.isSpecialTexture(originalWithDirs)) {
			// load alpha mask of appropriate size
			BufferedImage maskImage = loadLeafMaskImage(defaultMask, size * 2);
			int scale = size * 2 / maskImage.getWidth();
			
			for (int x = 0; x < overlayIcon.getWidth(); x++) {
				for (int y = 0; y < overlayIcon.getHeight(); y++) {
					long origPixel = overlayIcon.getRGB(x, y) & 0xFFFFFFFFl;
					long maskPixel = maskImage.getRGB(x / scale, y / scale) & 0xFF000000l | 0x00FFFFFF;
					overlayIcon.setRGB(x, y, (int) (origPixel & maskPixel));
				}
			}
		}
		
		generatedCounter++;
		return new BufferedImageResource(overlayIcon);
	}
	
	/** Loads the alpha mask of the given type and size. If a mask of the exact size can not be found,
	 *  will try to load progressively smaller masks down to 16x16
	 * @param type mask type
	 * @param size texture size
	 * @return alpha mask
	 */
	protected BufferedImage loadLeafMaskImage(String type, int size) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		IResource maskResource = null;
		
		while (maskResource == null && size >= 16) {
			try {
				maskResource = resourceManager.getResource(new ResourceLocation(String.format("betterfoliage:textures/blocks/leafmask_%d_%s.png", size, type)));
			} catch (Exception e) {}
			size /= 2;
		}
		
		try {
			return maskResource == null ? null : ImageIO.read(maskResource.getInputStream());
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	@SubscribeEvent
	public void handleTextureReload(Pre event) {
		super.handleTextureReload(event);
		if (event.map.getTextureType() != 0) return;
		generatedCounter = 0;
		drawnCounter = 0;
	}

	@Override
	@SubscribeEvent
	public void endTextureReload(Post event) {
		super.endTextureReload(event);
		if (event.map.getTextureType() != 0) return;
		BetterFoliage.log.info(String.format("Found %d pre-drawn leaf textures", drawnCounter));
		BetterFoliage.log.info(String.format("Found %d leaf textures", generatedCounter));
	}

	
}
