package mods.betterfoliage.client.texture.generator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Base class for texture generators. Registers itself as a domain resource manager for the duration of block texture stitching.
 * @author octarine-noise
 *
 */
@SideOnly(Side.CLIENT)
public abstract class BlockTextureGenerator implements IResourceManager {

	/** Resource domain name of generated textures */
	public String domainName;
	
	/** Resource location for fallback texture (if the generation process fails) */
	public ResourceLocation missingResource;
	
	/** Texture atlas for block textures used in the current run */
	public TextureMap blockTextures;
	
	public BlockTextureGenerator(String domainName, ResourceLocation missingResource) {
		this.domainName = domainName;
		this.missingResource = missingResource;
	}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		blockTextures = event.map;

		Map<String, IResourceManager> domainManagers = CodeRefs.fDomainResourceManagers.getInstanceField(Minecraft.getMinecraft().getResourceManager());
		if (domainManagers == null) {
			BetterFoliage.log.warn("Failed to inject texture generator");
			return;
		}
		domainManagers.put(domainName, this);
	}
	
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		blockTextures = null;
		
		// don't leave a mess
		Map<String, IResourceManager> domainManagers = CodeRefs.fDomainResourceManagers.getInstanceField(Minecraft.getMinecraft().getResourceManager());
		if (domainManagers != null) domainManagers.remove(domainName);
	}
	
	public Set<String> getResourceDomains() {
		return ImmutableSet.<String>of(domainName);
	}
	
	public List<IResource> getAllResources(ResourceLocation resource) throws IOException {
		return ImmutableList.<IResource>of(getResource(resource));
	}
	
	public IResource getMissingResource() throws IOException {
		return Minecraft.getMinecraft().getResourceManager().getResource(missingResource);
	}
	
	public ResourceLocation unwrapResource(ResourceLocation wrapped) {
		return new ResourceLocation(wrapped.getResourcePath().substring(9));
	}
	
	protected static int blendRGB(int rgbOrig, int rgbBlend, int weightOrig, int weightBlend) {
		int r = (((rgbOrig >> 16) & 0xFF) * weightOrig + ((rgbBlend >> 16) & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int g = (((rgbOrig >> 8) & 0xFF) * weightOrig + ((rgbBlend >> 8) & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int b = ((rgbOrig & 0xFF) * weightOrig + (rgbBlend & 0xFF) * weightBlend) / (weightOrig + weightBlend);
		int a = (rgbOrig >> 24) & 0xFF;
		int result = (int) (a << 24 | r << 16 | g << 8 | b);
		return result;
	}
}
