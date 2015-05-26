package mods.betterfoliage.client.texture.generator;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Texture generator base class for textures based on leaf blocks.
 * Supports loading from resource packs instead of generating if available.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public abstract class LeafGeneratorBase extends BlockTextureGenerator {

	@SideOnly(Side.CLIENT)
	public static class TextureGenerationException extends Exception {
		private static final long serialVersionUID = 7339757761980002651L;
	};
	
	/** Format string of pre-drawn texture location */
	public String handDrawnLocationFormat;
	
	/** Format string of alpha mask location */
	public String maskImageLocationFormat;
	
	/** Resource domain name of pre-drawn textures */
	public String nonGeneratedDomain;
	
	/** Number of textures generated in the current run */
	public int generatedCounter = 0;
	
	/** Number of pre-drawn textures found in the current run */
	public int drawnCounter = 0;
	
	public LeafGeneratorBase(String domain, String nonGeneratedDomain, String handDrawnLocationFormat, String maskImageLocationFormat, ResourceLocation missingResource) {
		super(domain, missingResource);
		this.nonGeneratedDomain = nonGeneratedDomain;
		this.handDrawnLocationFormat = handDrawnLocationFormat;
		this.maskImageLocationFormat = maskImageLocationFormat;
	}

	@Override
	public IResource getResource(ResourceLocation resourceLocation) throws IOException {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		ResourceLocation originalNoDirs = unwrapResource(resourceLocation);
		ResourceLocation originalWithDirs = new ResourceLocation(originalNoDirs.getResourceDomain(), "textures/" + originalNoDirs.getResourcePath());
		
		// check for provided texture
		ResourceLocation handDrawnLocation = new ResourceLocation(nonGeneratedDomain, String.format(handDrawnLocationFormat, originalNoDirs.getResourceDomain(), originalNoDirs.getResourcePath().substring(7))); 
		if (ResourceUtils.resourceExists(handDrawnLocation)) {
			drawnCounter++;
			return resourceManager.getResource(handDrawnLocation);
		}
		
		// use animation metadata as-is
		if (resourceLocation.getResourcePath().toLowerCase().endsWith(".mcmeta")) return resourceManager.getResource(originalWithDirs);
		
		// generate our own
		if (!ResourceUtils.resourceExists(originalWithDirs)) return getMissingResource();
		
		BufferedImage result;
		try {
			result = generateLeaf(originalWithDirs);
		} catch (TextureGenerationException e) {
			return getMissingResource();
		}
		generatedCounter++;
		return new BufferedImageResource(resourceLocation, originalWithDirs, result);
	}
	
	protected abstract BufferedImage generateLeaf(ResourceLocation originalWithDirs) throws IOException, TextureGenerationException;
	
	/** Loads the alpha mask of the given type and size. If a mask of the exact size can not be found,
	 *  will try to load progressively smaller masks down to 16x16
	 * @param type mask type
	 * @param size texture size
	 * @return alpha mask
	 */
	protected BufferedImage loadLeafMaskImage(String type, int size) {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		IResource maskResource = null;
		
		while (maskResource == null && size >= 1) {
			try {
				maskResource = resourceManager.getResource(new ResourceLocation(String.format(maskImageLocationFormat, size, type)));
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
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		super.handleTextureReload(event);
		generatedCounter = 0;
		drawnCounter = 0;
	}

}
