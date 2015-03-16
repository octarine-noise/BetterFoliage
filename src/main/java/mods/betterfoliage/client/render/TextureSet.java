package mods.betterfoliage.client.render;

import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Holds an indexed set of textures
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class TextureSet {

	/** Icon array */
	public TextureAtlasSprite[] icons = new TextureAtlasSprite[16];
	
	/** Icon to use if no textures could be loaded */
	public TextureAtlasSprite missing;
	
	/** Number of successfully loaded icons */
	public int numLoaded = 0;
	
	/** Resource domain of icons */
	String domain;
	
	/** Format string of icon paths */
	String path;
	
	/**
	 * @param domain resource domain of icons
	 * @param path format string of icon paths, should have a single <b>%d</b> in it
	 */
	public TextureSet(String domain, String path) {
		this.domain = domain;
		this.path = path;
	}
	
	/** Register the texture set in the atlas. Will try to load up to 16 variants. 
	 * @param register
	 */
	public void registerSprites(TextureMap register) {
		numLoaded = 0;
		for (int idx = 0; idx < 16; idx++) {
			icons[idx] = null;
			// if the path contains a domain, use that to check if the resource exists
			String resolvedDomain = path.contains(":") ? new ResourceLocation(path).getResourceDomain() : domain;
			String resolvedPath = String.format("textures/" + (path.contains(":") ? new ResourceLocation(path).getResourcePath() : path) + ".png", idx);
			if (ResourceUtils.resourceExists(new ResourceLocation(resolvedDomain, resolvedPath)))
				icons[numLoaded++] = register.registerSprite(new ResourceLocation(domain + ":" + String.format(path, idx)));
		}
		missing = register.getMissingSprite();
	}
	
	/**
	 * @param variation texture index (will wrap around if value is large)
	 * @return specified texture from the set
	 */
	public TextureAtlasSprite get(int variation) {
		return numLoaded == 0 ? missing : icons[variation % numLoaded];
	}
	
}
