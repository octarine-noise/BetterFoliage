package mods.betterfoliage.client;

import java.util.List;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.event.PostLoadModelDefinitionsEvent;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.WindTracker;
import mods.betterfoliage.client.render.impl.EntityFXFallingLeaves;
import mods.betterfoliage.client.render.impl.EntityFXRisingSoul;
import mods.betterfoliage.client.render.impl.RenderBlockDirtWithAlgae;
import mods.betterfoliage.client.render.impl.RenderBlockCactus;
import mods.betterfoliage.client.render.impl.RenderBlockDirtWithLog;
import mods.betterfoliage.client.render.impl.RenderBlockLogs;
import mods.betterfoliage.client.render.impl.RenderBlockSandWithCoral;
import mods.betterfoliage.client.render.impl.RenderBlockGrass;
import mods.betterfoliage.client.render.impl.RenderBlockLeaves;
import mods.betterfoliage.client.render.impl.RenderBlockLilypad;
import mods.betterfoliage.client.render.impl.RenderBlockMycelium;
import mods.betterfoliage.client.render.impl.RenderBlockNetherrack;
import mods.betterfoliage.client.render.impl.RenderBlockReed;
import mods.betterfoliage.client.texture.GrassTextures;
import mods.betterfoliage.client.texture.LeafTextures;
import mods.betterfoliage.client.texture.LogTextures;
import mods.betterfoliage.client.texture.SoulParticleTextures;
import mods.betterfoliage.client.texture.generator.LeafGenerator;
import mods.betterfoliage.client.texture.generator.ReedGenerator;
import mods.betterfoliage.client.texture.generator.ShortGrassGenerator;
import mods.betterfoliage.client.texture.models.VanillaMapping;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.collect.Lists;

public class BetterFoliageClient {

	public static ResourceLocation missingTexture = new ResourceLocation(BetterFoliage.DOMAIN, "textures/blocks/missing_leaf.png");
	
	public static List<BFAbstractRenderer> renderers = Lists.newLinkedList();
	
	public static LeafTextures leafRegistry = new LeafTextures();
	public static LeafGenerator leafGenerator = new LeafGenerator();
	public static SoulParticleTextures soulParticles = new SoulParticleTextures();
	public static GrassTextures grassRegistry = new GrassTextures();
	public static LogTextures logRegistry = new LogTextures();
	public static WindTracker wind = new WindTracker();
	
	public static void postInit() {
		FMLCommonHandler.instance().bus().register(new KeyHandler());
		FMLCommonHandler.instance().bus().register(new Config());
		FMLCommonHandler.instance().bus().register(new BetterFoliageClient());
		
		BetterFoliage.log.info("Registering renderers");
		registerRenderer(new RenderBlockLeaves());
		registerRenderer(new RenderBlockGrass());
		registerRenderer(new RenderBlockCactus());
		registerRenderer(new RenderBlockMycelium());
		registerRenderer(new RenderBlockLilypad());
		registerRenderer(new RenderBlockDirtWithAlgae());
		registerRenderer(new RenderBlockSandWithCoral());
		registerRenderer(new RenderBlockReed());
		registerRenderer(new RenderBlockNetherrack());
		registerRenderer(new RenderBlockLogs());
		registerRenderer(new RenderBlockDirtWithLog());
		
		MinecraftForge.EVENT_BUS.register(wind);
		FMLCommonHandler.instance().bus().register(wind);
		
		MinecraftForge.EVENT_BUS.register(Config.leaves);
		MinecraftForge.EVENT_BUS.register(Config.crops);
		MinecraftForge.EVENT_BUS.register(Config.dirt);
		MinecraftForge.EVENT_BUS.register(Config.grass);
		MinecraftForge.EVENT_BUS.register(Config.logs);
		
		BetterFoliage.log.info("Registering texture generators");
		MinecraftForge.EVENT_BUS.register(soulParticles);
		MinecraftForge.EVENT_BUS.register(leafGenerator);
		MinecraftForge.EVENT_BUS.register(leafRegistry);
		leafRegistry.leafMappings.add(new VanillaMapping("minecraft:models/block/leaves", "all"));
		leafRegistry.leafMappings.add(new VanillaMapping("minecraft:models/block/cube", "all"));
		MinecraftForge.EVENT_BUS.register(grassRegistry);
		grassRegistry.grassMappings.add(new VanillaMapping("minecraft:models/block/grass", "top"));
		grassRegistry.grassMappings.add(new VanillaMapping("minecraft:models/block/cube_bottom_top", "top"));
		MinecraftForge.EVENT_BUS.register(logRegistry);
		logRegistry.logSideMappings.add(new VanillaMapping("minecraft:models/block/cube_column", "side"));
		logRegistry.logSideMappings.add(new VanillaMapping("minecraft:models/block/column_side", "side"));
		logRegistry.logEndMappings.add(new VanillaMapping("minecraft:models/block/cube_column", "end"));
		logRegistry.logEndMappings.add(new VanillaMapping("minecraft:models/block/column_side", "end"));
		
		MinecraftForge.EVENT_BUS.register(new ReedGenerator("bf_reed_bottom", missingTexture, true));
		MinecraftForge.EVENT_BUS.register(new ReedGenerator("bf_reed_top", missingTexture, false));
		MinecraftForge.EVENT_BUS.register(new ShortGrassGenerator("bf_shortgrass", missingTexture, false));
		MinecraftForge.EVENT_BUS.register(new ShortGrassGenerator("bf_shortgrass_snow", missingTexture, true));
		
		ShadersModIntegration.init();
	}

	/** Called from transformed {@link WorldClient#doVoidFogParticles(int, int, int)} method right before the end of the <b>for</b> loop.
	 * @param world
	 * @param blockState
	 * @param pos
	 */
	public static void onRandomDisplayTick(World world, IBlockState blockState, BlockPos pos) {
	    Block block = blockState.getBlock();
	    if (Config.soulFXEnabled) {
	        if (block == Blocks.soul_sand && Math.random() < Config.soulFXChance) {
	            Minecraft.getMinecraft().effectRenderer.addEffect(new EntityFXRisingSoul(world, pos));
	            return;
	        }
	    }
	    if (Config.leafFXEnabled) {
	        if (Config.leaves.matchesID(block) && world.isAirBlock(pos.add(0, -1, 0)) && Math.random() < Config.leafFXChance) {
	        	new EntityFXFallingLeaves(world, blockState, pos).addToRenderer(Minecraft.getMinecraft().effectRenderer);
	            return;
	        }
	    }
	}
	
	public static boolean shouldRenderBlockSideOverride(boolean original, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
	    return original || (Config.logsEnabled && Config.logs.matchesID(blockAccess.getBlockState(pos).getBlock()));
	}
	
	public static float getAmbientOcclusionLightValueOverride(float original, Block block) {
	    if (Config.logsEnabled && Config.logs.matchesID(block)) return 1.0f;
	    return original;
	}
	
    public static boolean getUseNeighborBrightnessOverride(boolean original, Block block) {
        return original || (Config.logsEnabled && Config.logs.matchesID(block));
    }
    
	public static boolean canRenderBlockInLayer(Block block, EnumWorldBlockLayer layer) {
	    return block.canRenderInLayer(layer) || layer == EnumWorldBlockLayer.CUTOUT_MIPPED;
	}
	
	public static boolean renderWorldBlock(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer worldRenderer, EnumWorldBlockLayer layer) {
		boolean canBlockRender = state.getBlock().canRenderInLayer(layer);
		boolean hasRendered = false;
		boolean useAO = Minecraft.isAmbientOcclusionEnabled() && state.getBlock().getLightValue() == 0;
		for(BFAbstractRenderer renderer : renderers) if (renderer.isBlockEligible(blockAccess, state, pos)) {
			if (!renderer.isStandardRenderBlocked && canBlockRender) hasRendered = dispatcher.renderBlock(state, pos, blockAccess, worldRenderer);
			if (renderer.renderBlock(dispatcher, blockAccess, state, pos, worldRenderer, useAO, layer)) hasRendered = true;
			return hasRendered;
		}
	    return canBlockRender ? dispatcher.renderBlock(state, pos, blockAccess, worldRenderer) : false;
	}
	
    public static void onAfterLoadModelDefinitions(ModelLoader loader) {
        MinecraftForge.EVENT_BUS.post(new PostLoadModelDefinitionsEvent(loader));
    }
	   
    public static void registerRenderer(BFAbstractRenderer renderer) {
        MinecraftForge.EVENT_BUS.register(renderer);
        renderers.add(renderer);
        renderer.onConfigReload();
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void handleConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) {
        for (BFAbstractRenderer renderer : renderers) renderer.onConfigReload();
    }
}
