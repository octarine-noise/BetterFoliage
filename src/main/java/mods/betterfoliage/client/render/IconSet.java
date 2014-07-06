package mods.betterfoliage.client.render;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/** Loads an indexed set of textures
 * @author octarine-noise
 */
public class IconSet {

	/** Icon array */
	public IIcon[] icons = new IIcon[16];
	
	/** Number of successfully loaded icons*/
	public int numLoaded = 0;
	
	/** Resource domain of icons */
	String domain;
	
	/** Format string of icon paths */
	String path;
	
	public IconSet(String domain, String path) {
		this.domain = domain;
		this.path = path;
	}
	
	public void registerIcons(IIconRegister register) {
		numLoaded = 0;
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		
		for (int idx = 0; idx < 16; idx++) {
			icons[idx] = null;
			// if the path contains a domain, use that to check if the resource exists
			String resolvedDomain = path.contains(":") ? new ResourceLocation(path).getResourceDomain() : domain;
			String resolvedPath = String.format("textures/blocks/" + (path.contains(":") ? new ResourceLocation(path).getResourcePath() : path) + ".png", idx);
			try {
				IResource resource = manager.getResource(new ResourceLocation(resolvedDomain, resolvedPath));
				if (resource != null) icons[numLoaded++] = register.registerIcon(domain + ":" + String.format(path, idx));
			} catch (IOException e) {
			}
		}
	}
	
	public IIcon get(int variation) {
		return numLoaded == 0 ? null : icons[variation % numLoaded];
	}
	
	public boolean hasIcons() {
		return numLoaded > 0;
	}
}
