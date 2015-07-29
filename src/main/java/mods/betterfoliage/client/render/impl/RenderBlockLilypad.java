package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.FaceCrossedQuads;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLilypad extends BFAbstractRenderer {

    public TextureSet lilypadFlowers = new TextureSet("bettergrassandleaves", "blocks/better_lilypad_flower_%d");
    public TextureSet lilypadRoots = new TextureSet("bettergrassandleaves", "blocks/better_lilypad_roots_%d");
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.lilypadEnabled && blockState.getBlock() == Blocks.waterlily;
    }

    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
        int chanceVariation = getSemiRandomFromPos(pos, 0);
        int iconVariation = getSemiRandomFromPos(pos, 1);
        int offsetVariation = getSemiRandomFromPos(pos, 2);
        
        double halfSize = 0.2;
        double halfHeight = 0.3;
        Double3 offset = random.getCircleXZ(Config.lilypadHOffset, offsetVariation);
        Double3 faceCenter = new Double3(pos).add(0.5, 0.0, 0.5);
        
        ShadersModIntegration.startGrassQuads(worldRenderer);
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads roots = FaceCrossedQuads.createTranslated(faceCenter.add(0, 0.015, 0), EnumFacing.DOWN, offset, halfSize, halfHeight);
        roots.setTexture(lilypadRoots.get(iconVariation), 2).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        ShadersModIntegration.finish(worldRenderer);
        
        if (chanceVariation < Config.lilypadChance) {
            FaceCrossedQuads flower = FaceCrossedQuads.createTranslated(faceCenter.add(0, 0.02, 0), EnumFacing.UP, offset, halfSize, halfHeight);
            flower.setTexture(lilypadFlowers.get(iconVariation), 0).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        }
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        lilypadFlowers.registerSprites(event.map);
        lilypadRoots.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d lilypad flower textures", lilypadFlowers.numLoaded));
        BetterFoliage.log.info(String.format("Found %d lilypad root textures", lilypadRoots.numLoaded));
    }
    
}
