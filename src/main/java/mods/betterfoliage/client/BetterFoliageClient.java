package mods.betterfoliage.client;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.impl.RenderBlockBetterAlgae;
import mods.betterfoliage.client.render.impl.RenderBlockBetterCactus;
import mods.betterfoliage.client.render.impl.RenderBlockBetterCoral;
import mods.betterfoliage.client.render.impl.RenderBlockBetterGrass;
import mods.betterfoliage.client.render.impl.RenderBlockBetterLeaves;
import mods.betterfoliage.client.render.impl.RenderBlockBetterLilypad;
import mods.betterfoliage.client.render.impl.RenderBlockBetterReed;
import mods.betterfoliage.client.resource.BlockTextureGenerator;
import mods.betterfoliage.client.resource.HalfTextureResource;
import mods.betterfoliage.client.resource.LeafTextureGenerator;
import mods.betterfoliage.client.resource.ShortGrassTextureResource;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Maps;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class BetterFoliageClient {

	public static Map<Integer, IRenderBlockDecorator> decorators = Maps.newHashMap();
	public static LeafTextureGenerator leafGenerator;
	
	public static BlockMatcher leaves = new BlockMatcher();
	public static BlockMatcher crops = new BlockMatcher();
	
	public static void preInit() {
		FMLCommonHandler.instance().bus().register(new KeyHandler());
		
		BetterFoliage.log.info("Registering renderers");
		registerRenderer(new RenderBlockBetterLeaves());
		registerRenderer(new RenderBlockBetterGrass());
		registerRenderer(new RenderBlockBetterCactus());
		registerRenderer(new RenderBlockBetterLilypad());
		registerRenderer(new RenderBlockBetterReed());
		registerRenderer(new RenderBlockBetterAlgae());
		registerRenderer(new RenderBlockBetterCoral());
		
		leaves.load(new File(BetterFoliage.configDir, "classesLeaves.cfg"), new ResourceLocation("betterfoliage:classesLeavesDefault.cfg"));
		MinecraftForge.EVENT_BUS.register(leaves);
		
		crops.load(new File(BetterFoliage.configDir, "classesCrops.cfg"), new ResourceLocation("betterfoliage:classesCropsDefault.cfg"));
		MinecraftForge.EVENT_BUS.register(crops);
		
		BetterFoliage.log.info("Registering leaf texture generator");
		leafGenerator = new LeafTextureGenerator();
		MinecraftForge.EVENT_BUS.register(leafGenerator);
		
		MinecraftForge.EVENT_BUS.register(new BlockTextureGenerator("bf_reed_bottom", new ResourceLocation("betterfoliage", "textures/blocks/missing_leaf.png")) {
			@Override
			public IResource getResource(ResourceLocation var1) throws IOException {
				return new HalfTextureResource(unwrapResource(var1), true, getMissingResource());
			}
		});
		MinecraftForge.EVENT_BUS.register(new BlockTextureGenerator("bf_reed_top", new ResourceLocation("betterfoliage", "textures/blocks/missing_leaf.png")) {
			@Override
			public IResource getResource(ResourceLocation var1) throws IOException {
				return new HalfTextureResource(unwrapResource(var1), false, getMissingResource());
			}
		});
		MinecraftForge.EVENT_BUS.register(new BlockTextureGenerator("bf_shortgrass", new ResourceLocation("betterfoliage", "textures/blocks/missing_leaf.png")) {
			@Override
			public IResource getResource(ResourceLocation var1) throws IOException {
				return new ShortGrassTextureResource(unwrapResource(var1), getMissingResource());
			}
		});

		MinecraftForge.EVENT_BUS.register(new BetterFoliageClient());
		
		ShadersModIntegration.init();
	}

	public static boolean isLeafTexture(TextureAtlasSprite icon) {
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

}
