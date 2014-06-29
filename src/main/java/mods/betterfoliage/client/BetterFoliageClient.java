package mods.betterfoliage.client;

import java.io.File;
import java.util.Map;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.impl.RenderBlockBetterCactus;
import mods.betterfoliage.client.render.impl.RenderBlockBetterGrass;
import mods.betterfoliage.client.render.impl.RenderBlockBetterLeaves;
import mods.betterfoliage.client.render.impl.RenderBlockBetterLilypad;
import mods.betterfoliage.client.resource.ILeafTextureRecognizer;
import mods.betterfoliage.client.resource.LeafTextureGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class BetterFoliageClient implements ILeafTextureRecognizer {

	public static Map<Integer, IRenderBlockDecorator> decorators = Maps.newHashMap();
	public static Set<Class<?>> blockLeavesClasses = Sets.newHashSet();
	public static LeafTextureGenerator leafGenerator;
	
	public static void preInit() {
		FMLCommonHandler.instance().bus().register(new KeyHandler());
		
		BetterFoliage.log.info("Registering renderers");
		registerRenderer(new RenderBlockBetterLeaves());
		registerRenderer(new RenderBlockBetterGrass());
		registerRenderer(new RenderBlockBetterCactus());
		registerRenderer(new RenderBlockBetterLilypad());
		
		blockLeavesClasses.add(BlockLeavesBase.class);
		addLeafBlockClass("forestry.arboriculture.gadgets.BlockLeaves");
		addLeafBlockClass("thaumcraft.common.blocks.BlockMagicalLeaves");
		
		BetterFoliage.log.info("Registering leaf texture generator");
		leafGenerator = new LeafTextureGenerator();
		MinecraftForge.EVENT_BUS.register(leafGenerator);
		leafGenerator.recognizers.add(new BetterFoliageClient());
		leafGenerator.loadLeafMappings(new File(BetterFoliage.configDir, "leafMask.properties"));
	}

	public boolean isLeafTexture(TextureAtlasSprite icon) {
		String resourceLocation = icon.getIconName();
		if (resourceLocation.startsWith("forestry:leaves/")) return true;
		return false;
	}
	
	public static int getRenderTypeOverride(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		// universal sign for DON'T RENDER ME!
		if (original == -1) return original;
		
		for (Map.Entry<Integer, IRenderBlockDecorator> entry : decorators.entrySet())
			if (entry.getValue().isBlockAccepted(blockAccess, x, y, z, block, original))
				return entry.getKey();
		
		return original;
	}
	
	public static void registerRenderer(IRenderBlockDecorator decorator) {
		int renderId = RenderingRegistry.getNextAvailableRenderId();
		decorators.put(renderId, decorator);
		RenderingRegistry.registerBlockHandler(renderId, decorator);
		MinecraftForge.EVENT_BUS.register(decorator);
		decorator.init();
	}

	protected static void addLeafBlockClass(String className) {
		try {
			blockLeavesClasses.add(Class.forName(className));
		} catch(ClassNotFoundException e) {
		}
	}
}
