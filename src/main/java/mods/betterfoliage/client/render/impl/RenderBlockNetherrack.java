package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
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
public class RenderBlockNetherrack extends BFAbstractRenderer {

    public TextureSet netherrackVineIcons = new TextureSet("bettergrassandleaves", "blocks/better_netherrack_%d");
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.netherrackEnabled && blockState.getBlock() == Blocks.netherrack && blockAccess.getBlockState(pos.down()).getBlock().isOpaqueCube();
    }

    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int textureVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.netherrackSize;
        double halfHeight = 0.5 * random.getRange(Config.netherrackHeightMin, Config.netherrackHeightMax, offsetVariation);
        Double3 offset = random.getCircleXZ(Config.netherrackHOffset, offsetVariation);
        Double3 faceCenter = new Double3(pos).add(0.5, 0.0, 0.5);
        
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        FaceCrossedQuads vines = FaceCrossedQuads.createTranslated(faceCenter, EnumFacing.DOWN, offset, halfSize, halfHeight);
        vines.setTexture(netherrackVineIcons.get(textureVariation), 2).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        netherrackVineIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d netherrack vine textures", netherrackVineIcons.numLoaded));
    }
    
}
