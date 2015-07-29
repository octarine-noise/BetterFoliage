package mods.betterfoliage.client.render.impl;

import java.util.Random;

import mods.betterfoliage.BetterFoliage;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockDirtWithAlgae extends BFAbstractRenderer {

    public static int seedOffset = 1;
    
    public TextureSet algaeIcons = new TextureSet("bettergrassandleaves", "blocks/better_algae_%d");
    public NoiseGeneratorSimplex noise;
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	if (!Config.algaeEnabled) return false;
        if (noise == null) return false;
        
        if (!Config.dirt.matchesID(blockState.getBlock())) return false;
        if (!Config.algaeBiomeList.contains(blockAccess.getBiomeGenForCoords(pos).biomeID)) return false;
        
        if (blockAccess.getBlockState(pos.up()).getBlock().getMaterial() != Material.water) return false;
        if (blockAccess.getBlockState(pos.up(2)).getBlock().getMaterial() != Material.water) return false;
        
        int terrainVariation = MathHelper.floor_double((noise.func_151605_a(pos.getX(), pos.getZ()) + 1.0) * 32.0);
        if (terrainVariation >= Config.algaePopulation) return false;
        
        return true;
    }
    
    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int textureVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.algaeSize;
        double halfHeight = 0.5 * random.getRange(Config.algaeHeightMin, Config.algaeHeightMax, offsetVariation);
        Double3 offset = random.getCircleXZ(Config.algaeHOffset, offsetVariation);
        Double3 faceCenter = new Double3(pos).add(0.5, 1.0, 0.5);
        
        ShadersModIntegration.startGrassQuads(worldRenderer);
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads algae = FaceCrossedQuads.createTranslated(faceCenter, EnumFacing.UP, offset, halfSize, halfHeight);
        algae.setTexture(algaeIcons.get(textureVariation), 0).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        algaeIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d algae textures", algaeIcons.numLoaded));
    }
    
    @SubscribeEvent
    public void handleWorldLoad(WorldEvent.Load event) {
        noise = new NoiseGeneratorSimplex(new Random(event.world.getWorldInfo().getSeed() + seedOffset));
    }
}
