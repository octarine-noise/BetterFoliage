package mods.betterfoliage.client.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import mods.betterfoliage.client.resource.LeafTextureEnumerator.LeafTextureFoundEvent;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class LeafGeneratorBase extends BlockTextureGenerator {

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
		ResourceLocation originalWithDirs = new ResourceLocation(originalNoDirs.getResourceDomain(), "textures/blocks/" + originalNoDirs.getResourcePath());
		
		// check for provided texture
		ResourceLocation handDrawnLocation = new ResourceLocation(nonGeneratedDomain, String.format(handDrawnLocationFormat, originalNoDirs.getResourceDomain(), originalNoDirs.getResourcePath())); 
		if (Utils.resourceExists(handDrawnLocation)) {
			drawnCounter++;
			return resourceManager.getResource(handDrawnLocation);
		}
		
		// generate our own
		if (!Utils.resourceExists(originalWithDirs)) return getMissingResource();
		
		BufferedImage result;
		try {
			result = generateLeaf(originalWithDirs);
		} catch (TextureGenerationException e) {
			return getMissingResource();
		}
		generatedCounter++;
		return new BufferedImageResource(result);
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
	public void handleTextureReload(Pre event) {
		super.handleTextureReload(event);
		if (event.map.getTextureType() != 0) return;
		generatedCounter = 0;
		drawnCounter = 0;
	}

	@SubscribeEvent
	public void handleRegisterTexture(LeafTextureFoundEvent event) {
		event.blockTextures.registerIcon(new ResourceLocation(domainName, event.icon.getIconName()).toString());
	}

}
