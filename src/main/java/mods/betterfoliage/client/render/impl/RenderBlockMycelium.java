package mods.betterfoliage.client.render.impl;

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
public class RenderBlockMycelium extends BFAbstractRenderer {

    public TextureSet myceliumIcons = new TextureSet("bettergrassandleaves", "blocks/better_mycel_%d");
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.myceliumEnabled && blockState.getBlock() == Blocks.mycelium && !blockAccess.getBlockState(pos.up()).getBlock().isOpaqueCube();
    }

    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
        boolean isSnowTop = blockAccess.getBlockState(pos.offset(EnumFacing.UP)).getBlock().getMaterial() == Material.snow;
        if (isSnowTop) return false;
        
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int textureVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.grassSize;
        double halfHeight = 0.5 * random.getRange(Config.grassHeightMin, Config.grassHeightMax, offsetVariation);
        Double3 offset = random.getCircleXZ(Config.grassHOffset, offsetVariation);
        Double3 faceCenter = new Double3(pos).add(0.5, 1.0, 0.5);
        
        ShadersModIntegration.startGrassQuads(worldRenderer);
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads mycelium = FaceCrossedQuads.createTranslated(faceCenter.add(0, isSnowTop ? 0.1 : 0, 0), EnumFacing.UP, offset, halfSize, halfHeight);
        mycelium.setTexture(myceliumIcons.get(textureVariation), 0).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        myceliumIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d mycelium textures", myceliumIcons.numLoaded));
    }
    
}
