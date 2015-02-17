package mods.betterfoliage.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Call hooks and helper methods for dealing with Shaders Mod.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class ShadersModIntegration {

	private static boolean hasShadersMod = false;
	private static int tallGrassEntityData;
	private static int leavesEntityData;
	private static Method pushEntity;
	private static Method popEntity;
	private static Field vertexBuilder;
	
	/** Hide constructor */
	private ShadersModIntegration() {}
	
	public static void init() {
        tallGrassEntityData = Block.blockRegistry.getIDForObject(Blocks.tallgrass) & 0xFFFF | Blocks.tallgrass.getRenderType() << 16;
        leavesEntityData = Block.blockRegistry.getIDForObject(Blocks.leaves) & 0xFFFF | Blocks.leaves.getRenderType() << 16;
		try {
			Class<?> classSVertexBuilder = Class.forName("shadersmod.client.SVertexBuilder");
			pushEntity = classSVertexBuilder.getMethod("pushEntity", long.class);
			popEntity = classSVertexBuilder.getMethod("popEntity", WorldRenderer.class);
			vertexBuilder = WorldRenderer.class.getDeclaredField("sVertexBuilder");
			hasShadersMod = true;
			BetterFoliage.log.info("ShadersMod found, integration enabled");
		} catch(Exception e) {
		    BetterFoliage.log.info("ShadersMod not found, integration disabled");
		}
		
	}
	
	/** Signal start of grass-type quads
	 */
	public static void startGrassQuads(WorldRenderer renderer) {
		if (!hasShadersMod) return;
		pushEntity(renderer, tallGrassEntityData);
	}

	/** Signal start of leaf-type quads
	 */
	public static void startLeavesQuads(WorldRenderer renderer) {
		if (!hasShadersMod) return;
		pushEntity(renderer, leavesEntityData);
	}
	
	public static void finish(WorldRenderer renderer) {
	    if (!hasShadersMod) return;
	    popEntity(renderer);
	}
	
	/** Call hook from transformed ShadersMod class
	 * @param original entity data of currently rendered block
	 * @param blockState block state
	 * @return entity data to use
	 */
	public static int getBlockIdOverride(int original, IBlockState blockState) {
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
	
	/** Push block ID data on the vertex builder's stack
	 * @param renderer
	 * @param data
	 */
	protected static void pushEntity(WorldRenderer renderer, long data) {
	    try {
	        Object builder = vertexBuilder.get(renderer);
            pushEntity.invoke(builder, data);
        } catch (Exception e) {
        }
	}
	
	/** Pop block ID data off the vertex builder's stack
	 * @param renderer
	 */
	protected static void popEntity(WorldRenderer renderer) {
	    try {
            popEntity.invoke(null, renderer);
        } catch (Exception e) {
        }
	}
	
}
