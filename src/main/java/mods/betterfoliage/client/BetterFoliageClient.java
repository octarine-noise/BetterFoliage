package mods.betterfoliage.client;

import java.io.File;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.BlockRenderTypeOverride;
import mods.betterfoliage.BlockRenderTypeOverride.IRenderTypeProvider;
import mods.betterfoliage.client.render.RenderBlockBetterGrass;
import mods.betterfoliage.client.render.RenderBlockBetterLeaves;
import mods.betterfoliage.client.resource.ILeafTextureRecognizer;
import mods.betterfoliage.client.resource.LeafTextureGenerator;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BetterFoliageClient implements IRenderTypeProvider, ILeafTextureRecognizer {

	public static int leavesRenderId;
	public static int grassRenderId;
	public static LeafTextureGenerator leafGenerator;
	public static Set<Class<?>> blockLeavesClasses = Sets.newHashSet();
	
	public static void preInit() {
		FMLCommonHandler.instance().bus().register(new KeyHandler());
		
		BetterFoliage.log.info("Registering renderers");
		leavesRenderId = RenderBlockBetterLeaves.register();
		grassRenderId = RenderBlockBetterGrass.register();
		BlockRenderTypeOverride.provider = new BetterFoliageClient();
		
		blockLeavesClasses.add(BlockLeavesBase.class);
		addLeafBlockClass("forestry.arboriculture.gadgets.BlockLeaves");
		addLeafBlockClass("thaumcraft.common.blocks.BlockMagicalLeaves");
		
		BetterFoliage.log.info("Registering leaf texture generator");
		leafGenerator = new LeafTextureGenerator();
		MinecraftForge.EVENT_BUS.register(leafGenerator);
		leafGenerator.recognizers.add(new BetterFoliageClient());
		leafGenerator.loadLeafMappings(new File(BetterFoliage.configDir, "leafMask.properties"));
	}
	
	protected static void addLeafBlockClass(String className) {
		try {
			blockLeavesClasses.add(Class.forName(className));
		} catch(ClassNotFoundException e) {
		}
	}

	public int getRenderType(Block block) {
		if (Config.grassEnabled && block instanceof BlockGrass) return grassRenderId;
		
		if (Config.leavesEnabled)
			for (Class<?> clazz : blockLeavesClasses)
				if (clazz.isAssignableFrom(block.getClass()))
					return leavesRenderId;
		
		return block.getRenderType();
	}

	public boolean isLeafTexture(TextureAtlasSprite icon) {
		String resourceLocation = icon.getIconName();
		if (resourceLocation.startsWith("forestry:leaves/")) return true;
		return false;
	}
}
