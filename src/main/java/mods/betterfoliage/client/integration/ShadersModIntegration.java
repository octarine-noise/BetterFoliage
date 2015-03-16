package mods.betterfoliage.client.integration;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Call hooks and helper methods for dealing with Shaders Mod.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class ShadersModIntegration extends AbstractModIntegration {

	private static boolean isAvailable = false;
	private static int tallGrassEntityData;
	private static int leavesEntityData;
	
	/** Hide constructor */
	private ShadersModIntegration() {}
	
	public static void init() {
		tallGrassEntityData = Block.blockRegistry.getIDForObject(Blocks.tallgrass) & 0xFFFF | Blocks.tallgrass.getRenderType() << 16;
		leavesEntityData = Block.blockRegistry.getIDForObject(Blocks.leaves) & 0xFFFF | Blocks.leaves.getRenderType() << 16;
		
		if (isAllAvailable(CodeRefs.shaders)) {
			isAvailable = true;
			BetterFoliage.log.info("Found Shaders Mod");
		}
	}
	
	/** Signal start of grass-type quads
	 */
	public static void startGrassQuads() {
		if (!isAvailable) return;
		setShadersEntityData(tallGrassEntityData);
	}

	/** Signal start of leaf-type quads
	 */
	public static void startLeavesQuads() {
		if (!isAvailable) return;
		setShadersEntityData(leavesEntityData);
	}
	
	/** Change the entity data (containing block ID) for the currently rendered block.
	 *  Quads drawn afterwards will have the altered data.
	 * @param data
	 */
	private static void setShadersEntityData(int data) {
		try {
			int[] entityData = CodeRefs.fShadersEntityData.getStaticField();
			int entityDataIndex = CodeRefs.fShadersEntityDataIndex.getStaticField();
			entityData[(entityDataIndex * 2)] = data;
		} catch (Exception e) {
		}
	}
	
	/** Call hook from transformed ShadersMod class
	 * @param original entity data of currently rendered block
	 * @param block the block
	 * @return entity data to use
	 */
	public static int getBlockIdOverride(int original, Block block) {
		if (Config.leaves.matchesID(original & 0xFFFF)) return leavesEntityData;
		if (Config.crops.matchesID(original & 0xFFFF)) return tallGrassEntityData;
		return original;
	}
	
	/**
	 * @param resource texture resource
	 * @return true if texture is a normal or specular map
	 */
	public static boolean isSpecialTexture(ResourceLocation resource) {
		return resource.getResourcePath().toLowerCase().endsWith("_n.png") || resource.getResourcePath().toLowerCase().endsWith("_s.png");
	}
}
