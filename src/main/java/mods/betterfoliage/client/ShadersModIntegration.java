package mods.betterfoliage.client;

import java.lang.reflect.Field;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/** Call hooks and helper methods for dealing with Shaders Mod.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class ShadersModIntegration {

	private static boolean hasShadersMod = false;
	private static int tallGrassEntityData;
	private static int leavesEntityData;
	private static Field shadersEntityData;
	private static Field shadersEntityDataIndex;
	
	/** Hide constructor */
	private ShadersModIntegration() {}
	
	public static void init() {
		tallGrassEntityData = Block.blockRegistry.getIDForObject(Blocks.tallgrass) & 0xFFFF | Blocks.tallgrass.getRenderType() << 16;
		leavesEntityData = Block.blockRegistry.getIDForObject(Blocks.leaves) & 0xFFFF | Blocks.leaves.getRenderType() << 16;
		
		try {
			Class<?> classShaders = Class.forName("shadersmodcore.client.Shaders");
			shadersEntityData = classShaders.getDeclaredField("entityData");
			shadersEntityDataIndex = classShaders.getDeclaredField("entityDataIndex");
			hasShadersMod = true;
		} catch(Exception e) {
		}
	}
	
	/** Signal start of grass-type quads
	 */
	public static void startGrassQuads() {
		if (!hasShadersMod) return;
		setShadersEntityData(tallGrassEntityData);
	}

	/** Signal start of leaf-type quads
	 */
	public static void startLeavesQuads() {
		if (!hasShadersMod) return;
		setShadersEntityData(leavesEntityData);
	}
	
	/** Change the entity data (containing block ID) for the currently rendered block.
	 *  Quads drawn afterwards will have the altered data.
	 * @param data
	 */
	private static void setShadersEntityData(int data) {
		try {
			int[] entityData = (int[]) shadersEntityData.get(null);
			int entityDataIndex = shadersEntityDataIndex.getInt(null);
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
