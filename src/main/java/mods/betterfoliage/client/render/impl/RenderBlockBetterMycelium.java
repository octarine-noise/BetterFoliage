package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.ShadersModIntegration;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.FaceCrossedQuads;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterMycelium extends BFAbstractRenderer {

    public TextureSet myceliumIcons = new TextureSet("bettergrassandleaves", "blocks/better_mycel_%d");
    
    @Override
    public boolean renderFeatureForBlock(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO) {
        if (!Config.grassEnabled) return false;
        if (blockAccess.getBlockState(pos.up()).getBlock().isOpaqueCube()) return false;
        if (blockState.getBlock() != Blocks.mycelium) return false;
        
        boolean isSnowTop = blockAccess.getBlockState(pos.offset(EnumFacing.UP)).getBlock().getMaterial() == Material.snow;
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int textureVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.grassSize;
        double halfHeight = 0.5 * random.getRange(Config.grassHeightMin, Config.grassHeightMax, offsetVariation);
        Double3 offset = random.getCircleXZ(Config.grassHOffset, offsetVariation);
        Double3 faceCenter = new Double3(pos).add(0.5, 1.0, 0.5);
        
        ShadersModIntegration.startGrassQuads(worldRenderer);
        shadingData.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads mycelium = FaceCrossedQuads.createTranslated(faceCenter.add(0, isSnowTop ? 0.1 : 0, 0), EnumFacing.UP, offset, halfSize, halfHeight);
        mycelium.setTexture(myceliumIcons.get(textureVariation), 0).setBrightness(shadingData).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        myceliumIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d mycelium textures", myceliumIcons.numLoaded));
    }
    
}
