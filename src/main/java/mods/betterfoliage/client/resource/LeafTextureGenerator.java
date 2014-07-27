package mods.betterfoliage.client.resource;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.util.Utils;
import mods.betterfoliage.loader.DeobfHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;

import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Generates rounded crossleaf textures for all registered normal leaf textures at stitch time.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class LeafTextureGenerator extends BlockTextureGenerator implements IIconRegister {

	public String nonGeneratedDomain = "betterfoliage";
	
	public int nonGeneratedCounter = 0;
	
	public LeafTextureGenerator() {
		super("bf_leaves_autogen", new ResourceLocation("betterfoliage", "textures/blocks/missing_leaf.png"));
	}

	public IResource getResource(ResourceLocation resourceLocation) throws IOException {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		ResourceLocation originalNoDirs = unwrapResource(resourceLocation);
		ResourceLocation originalWithDirs = new ResourceLocation(originalNoDirs.getResourceDomain(), "textures/blocks/" + originalNoDirs.getResourcePath());
		
		// check for provided texture
		ResourceLocation handDrawnLocation = new ResourceLocation(nonGeneratedDomain, String.format("textures/blocks/%s/%s", originalNoDirs.getResourceDomain(), originalNoDirs.getResourcePath())); 
		if (Utils.resourceExists(handDrawnLocation)) {
			nonGeneratedCounter++;
			return resourceManager.getResource(handDrawnLocation);
		}
		
		// Don't alter ShaderMod normal and specular maps
		if (originalWithDirs.getResourcePath().toLowerCase().endsWith("_n.png") || originalWithDirs.getResourcePath().toLowerCase().endsWith("_s.png")) {
			resourceManager.getResource(originalWithDirs);
		}
		
		// generate our own
		if (!Utils.resourceExists(originalWithDirs)) return getMissingResource();
		LeafTextureResource result = new LeafTextureResource(resourceManager.getResource(originalWithDirs));
		if (result.data != null) {
			counter++;
			return result;
		} else {
			return getMissingResource();
		}
	}

	/** Leaf blocks register their textures here. An extra texture will be registered in the atlas
	 *  for each, with the resource domain of this generator.
	 *  @return the originally registered {@link IIcon} already in the atlas
	 */
	public IIcon registerIcon(String resourceLocation) {
		IIcon original = blockTextures.getTextureExtry(resourceLocation);
		blockTextures.registerIcon(new ResourceLocation(domainName, resourceLocation).toString());
		BetterFoliage.log.debug(String.format("Found leaf texture: %s", resourceLocation));
		return original;
	}

	/** Iterates through all leaf blocks in the registry and makes them register
	 *  their textures to "sniff out" all leaf textures.
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onStitchStart(Pre event) {
		nonGeneratedCounter = 0;
		BetterFoliage.log.info("Reloading leaf textures");
		
		// register simple block textures
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while(iter.hasNext()) {
			Block block = iter.next();
			if (BetterFoliageClient.leaves.matchesClass(block)) {
				BetterFoliage.log.debug(String.format("Inspecting leaf block: %s", block.getClass().getName()));
				block.registerBlockIcons(this);
			}
		}
		
		// enumerate all registered textures, find leaf textures among them
		Map<String, TextureAtlasSprite> mapAtlas = null;
		mapAtlas = Utils.getField(blockTextures, DeobfHelper.transformElementSearge("mapRegisteredSprites"), Map.class);
		if (mapAtlas == null) mapAtlas = Utils.getField(blockTextures, "mapRegisteredSprites", Map.class);
		if (mapAtlas == null) {
			BetterFoliage.log.warn("Failed to reflect texture atlas, textures may be missing");
		} else {
			Set<String> foundLeafTextures = Sets.newHashSet();
			for (TextureAtlasSprite icon : mapAtlas.values())
				if (BetterFoliageClient.isLeafTexture(icon))
					foundLeafTextures.add(icon.getIconName());
			for (String resourceLocation : foundLeafTextures) {
				BetterFoliage.log.debug(String.format("Found non-block-registered leaf texture: %s", resourceLocation));
				blockTextures.registerIcon(new ResourceLocation(domainName, resourceLocation).toString());
			}
		}
	}

	@Override
	public void onStitchEnd(Post event) {
		BetterFoliage.log.info(String.format("Found %d pre-drawn leaf textures", nonGeneratedCounter));
		BetterFoliage.log.info(String.format("Generated %d leaf textures", counter));
	}

}
