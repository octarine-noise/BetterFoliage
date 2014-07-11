package mods.betterfoliage.client.resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class BlockTextureGenerator implements IResourceManager {

	/** Resource domain name of generated textures */
	public String domainName;
	
	/** Resource location for fallback texture (if the generation process fails) */
	public ResourceLocation missingResource;
	
	/** Texture atlas for block textures used in the current run */
	public TextureMap blockTextures;
	
	/** Number of textures generated in the current run */
	int counter = 0;
	
	public BlockTextureGenerator(String domainName, ResourceLocation missingResource) {
		this.domainName = domainName;
		this.missingResource = missingResource;
	}
	
	public void onStitchStart(TextureStitchEvent.Pre event) {}
	
	public void onStitchEnd(TextureStitchEvent.Post event) {}
	
	@SubscribeEvent
	public void handleTextureReload(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() != 0) return;
		
		blockTextures = event.map;
		counter = 0;
		Map<String, IResourceManager> domainManagers = Utils.getDomainResourceManagers();
		if (domainManagers == null) {
			BetterFoliage.log.warn("Failed to inject texture generator");
			return;
		}
		domainManagers.put(domainName, this);
		
		onStitchStart(event);
	}
	
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		blockTextures = null;
		if (event.map.getTextureType() != 0) return;
		
		// don't leave a mess
		Map<String, IResourceManager> domainManagers = Utils.getDomainResourceManagers();
		if (domainManagers != null) domainManagers.remove(domainName);
		
		onStitchEnd(event);
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
		return new ResourceLocation(wrapped.getResourcePath().substring(16));
	}
}
