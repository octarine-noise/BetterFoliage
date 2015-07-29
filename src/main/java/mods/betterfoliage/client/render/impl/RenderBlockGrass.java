package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.FaceCrossedQuads;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockGrass extends BFAbstractRenderer {

    public TextureSet grassIcons = new TextureSet("bettergrassandleaves", "blocks/better_grass_long_%d");
    public TextureSet snowGrassIcons = new TextureSet("bettergrassandleaves", "blocks/better_grass_snowed_%d");
    public TextureAtlasSprite grassGenIcon, snowGrassGenIcon;
    public static Color4 snowColor = Color4.fromARGB(255, 200, 200, 200);
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.grassEnabled && Config.grass.matchesID(blockState.getBlock()) && !blockAccess.getBlockState(pos.up()).getBlock().isOpaqueCube();
    }
    
    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
    	boolean isSnowTop = blockAccess.getBlockState(pos.offset(EnumFacing.UP)).getBlock().getMaterial() == Material.snow;
    	if (isSnowTop && !Config.grassSnowEnabled) return false;
    	
        // render round leaves
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int textureVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.grassSize;
        double halfHeight = 0.5 * random.getRange(Config.grassHeightMin, Config.grassHeightMax, offsetVariation);
        Double3 offset = random.getCircleXZ(Config.grassHOffset, offsetVariation);
        Double3 blockCenter = new Double3(pos).add(0.5, 0.5, 0.5);
        Color4 renderColor = isSnowTop ? snowColor : BetterFoliageClient.grassRegistry.getRenderColor(blockState, Color4.fromARGB(blockState.getBlock().colorMultiplier(blockAccess, pos, 0)).opaque());
        
        ShadersModIntegration.startGrassQuads(worldRenderer);
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads grass = FaceCrossedQuads.create(offset, halfSize, halfHeight);
        grass.setTexture(Config.grassUseGenerated ? (isSnowTop ? snowGrassGenIcon : grassGenIcon) : ( (isSnowTop ? snowGrassIcons : grassIcons).get(textureVariation) ), 0);
        grass.setBrightness(shading).setColor(shading, renderColor).render(worldRenderer, blockCenter);
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        grassIcons.registerSprites(event.map);
        snowGrassIcons.registerSprites(event.map);
        grassGenIcon = event.map.registerSprite(new ResourceLocation("bf_shortgrass:minecraft:blocks/tallgrass"));
        snowGrassGenIcon = event.map.registerSprite(new ResourceLocation("bf_shortgrass_snow:minecraft:blocks/tallgrass"));
        BetterFoliage.log.info(String.format("Found %d short grass textures", grassIcons.numLoaded));
        BetterFoliage.log.info(String.format("Found %d snowy grass textures", snowGrassIcons.numLoaded));
    }
    
}
