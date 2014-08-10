package mods.betterfoliage.client;

import java.lang.reflect.Field;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class ShadersModIntegration {

	private static boolean hasShadersMod = false;
	private static int tallGrassEntityData;
	private static int leavesEntityData;
	private static Field shadersEntityData;
	private static Field shadersEntityDataIndex;
	
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
	
	public static void startGrassQuads() {
		if (!hasShadersMod) return;
		setShadersEntityData(tallGrassEntityData);
	}

	public static void startLeavesQuads() {
		if (!hasShadersMod) return;
		setShadersEntityData(leavesEntityData);
	}
	
	private static void setShadersEntityData(int data) {
		try {
			int[] entityData = (int[]) shadersEntityData.get(null);
			int entityDataIndex = shadersEntityDataIndex.getInt(null);
			entityData[(entityDataIndex * 2)] = data;
		} catch (Exception e) {
		}
	}
	
	public static int getBlockIdOverride(int original, Block block) {
		if (BetterFoliageClient.leaves.matchesID(original & 0xFFFF)) return leavesEntityData;
		if (BetterFoliageClient.crops.matchesID(original & 0xFFFF)) return tallGrassEntityData;
		return original;
	}
	
	public static boolean isSpecialTexture(ResourceLocation resource) {
		return resource.getResourcePath().toLowerCase().endsWith("_n.png") || resource.getResourcePath().toLowerCase().endsWith("_s.png");
	}
}
