package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.BlockCrossedQuads;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.FaceCrossedQuads;
import mods.betterfoliage.client.render.impl.primitives.IQuadCollection;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
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
public class RenderBlockCactus extends BFAbstractRenderer {

    /** Valid directions of cactus arm */
    public static EnumFacing[] cactusDirections = new EnumFacing[]{ EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
    
    /** Inner radius of cactus stem */
    public static double cactusRadius = 0.4375;
    
    /** Texture of cactus middle */
    public TextureAtlasSprite cactusRoundIcon;
    
    /** Texture set of cactus arms */
    public TextureSet cactusSideIcons = new TextureSet("bettergrassandleaves", "blocks/better_cactus_arm_%d");
    
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.cactusEnabled && blockState.getBlock() == Blocks.cactus;
    }
    
    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int uvVariation = getSemiRandomFromPos(pos, 1);
        int iconVariation = getSemiRandomFromPos(pos, 2);
        double halfSize = 0.65;
        Double3 blockCenter = new Double3(pos).add(0.5, 0.5, 0.5);
        
        // render cactus center
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
        IQuadCollection cactusCenter = BlockCrossedQuads.create(random.getCircleXZ(0.1, offsetVariation), random.getCircleXZ(0.1, offsetVariation + 1), halfSize);
        cactusCenter.setTexture(cactusRoundIcon, uvVariation).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer, blockCenter);
        
        // render cactus arm
        EnumFacing sideFacing = cactusDirections[offsetVariation % 4];
        halfSize = 0.5;
        FaceCrossedQuads cactusArm = FaceCrossedQuads.createTranslated(blockCenter.add(new Double3(sideFacing).scale(cactusRadius)), sideFacing, random.getCircleXZ(0.2, offsetVariation), halfSize, halfSize);
        cactusArm.setTexture(cactusSideIcons.get(iconVariation), 0).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        cactusSideIcons.registerSprites(event.map);
        cactusRoundIcon = event.map.registerSprite(new ResourceLocation("bettergrassandleaves:blocks/better_cactus"));
        BetterFoliage.log.info(String.format("Found %d cactus side textures", cactusSideIcons.numLoaded));
    }

}
