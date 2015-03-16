package mods.betterfoliage.client.render;

import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Holds an indexed set of textures
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class IconSet {

	/** Icon array */
	public IIcon[] icons = new IIcon[16];
	
	/** Number of successfully loaded icons */
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
		for (int idx = 0; idx < 16; idx++) {
			icons[idx] = null;
			// if the path contains a domain, use that to check if the resource exists
			String resolvedDomain = path.contains(":") ? new ResourceLocation(path).getResourceDomain() : domain;
			String resolvedPath = String.format("textures/blocks/" + (path.contains(":") ? new ResourceLocation(path).getResourcePath() : path) + ".png", idx);
			if (ResourceUtils.resourceExists(new ResourceLocation(resolvedDomain, resolvedPath)))
				icons[numLoaded++] = register.registerIcon(domain + ":" + String.format(path, idx));
		}
	}
	
	public IIcon get(int variation) {
		return numLoaded == 0 ? null : icons[variation % numLoaded];
	}
	
	public boolean hasIcons() {
		return numLoaded > 0;
	}
}
