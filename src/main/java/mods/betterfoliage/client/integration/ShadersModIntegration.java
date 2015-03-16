package mods.betterfoliage.client.integration;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
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
        	BetterFoliage.log.info("ShadersMod found, integration enabled");
		}
	}
	
	/** Signal start of grass-type quads
	 */
	public static void startGrassQuads(WorldRenderer renderer) {
		if (!isAvailable) return;
		pushEntity(renderer, tallGrassEntityData);
	}

	/** Signal start of leaf-type quads
	 */
	public static void startLeavesQuads(WorldRenderer renderer) {
		if (!isAvailable) return;
		pushEntity(renderer, leavesEntityData);
	}
	
	public static void finish(WorldRenderer renderer) {
	    if (!isAvailable) return;
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
	        Object builder = CodeRefs.fSVertexBuilder.getInstanceField(renderer);
	        CodeRefs.mPushEntity_I.invokeInstanceMethod(builder, data);
        } catch (Exception e) {
        }
	}
	
	/** Pop block ID data off the vertex builder's stack
	 * @param renderer
	 */
	protected static void popEntity(WorldRenderer renderer) {
	    try {
	        Object builder = CodeRefs.fSVertexBuilder.getInstanceField(renderer);
	        CodeRefs.mPopEntity.invokeInstanceMethod(builder);
        } catch (Exception e) {
        }
	}
	
}
