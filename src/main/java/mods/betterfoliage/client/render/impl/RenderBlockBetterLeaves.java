package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.BlockCrossedQuads;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.texture.LeafTextureRegistry.LeafInfo;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockBetterLeaves extends BFAbstractRenderer {

    public TextureSet snowedLeavesIcons = new TextureSet("bettergrassandleaves", "blocks/better_leaves_snowed_%d");
    
    @Override
    public boolean renderFeatureForBlock(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO) {
        if (!Config.leavesEnabled) return false;
        if (!Config.leaves.matchesID(blockState.getBlock())) return false;
        
        // check if we have leaf textures for this block
        LeafInfo leafInfo = BetterFoliageClient.leafRegistry.leafInfoMap.get(blockState);
        if (leafInfo == null || leafInfo.roundLeafTexture == null) {
            return false;
        }

        // check conditions
        boolean isAir[] = new boolean[6];
        boolean hasAir = false;
        for (int i = 0; i < 6; i++) {
            isAir[i] = blockAccess.isAirBlock(pos.offset(EnumFacing.values()[i]));
            hasAir |= isAir[i];
        }
        boolean isSnowTop = blockAccess.getBlockState(pos.up()).getBlock().getMaterial() == Material.snow;
        if (!hasAir && !isSnowTop) return false;
        
        // render round leaves
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int uvVariation = getSemiRandomFromPos(pos, 1);
        double halfSize = 0.5 * Config.leavesSize;
        Color4 blockColor = Color4.fromARGB(blockState.getBlock().colorMultiplier(blockAccess, pos, 0)).opaque().multiply(0.75f);
        
        Double3 blockCenter = new Double3(pos).add(0.5, 0.5, 0.5);
        Double3 offset1 = random.getCylinderXZY(Config.leavesHOffset, Config.leavesVOffset, offsetVariation);
        Double3 offset2 = random.getCylinderXZY(Config.leavesHOffset, Config.leavesVOffset, offsetVariation + 1);

        ShadersModIntegration.startLeavesQuads(worldRenderer);
        shadingData.update(blockAccess, blockState.getBlock(), pos, useAO);
        BlockCrossedQuads roundLeaves = Config.leavesSkew ? BlockCrossedQuads.createSkewed(blockCenter, offset1, offset2, halfSize) : BlockCrossedQuads.createTranslated(blockCenter, offset1, halfSize);
        roundLeaves.setTexture(leafInfo.roundLeafTexture, uvVariation).setBrightness(shadingData).setColor(shadingData, blockColor).render(worldRenderer);
        if (isSnowTop) roundLeaves.setTexture(snowedLeavesIcons.get(uvVariation), 0).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        snowedLeavesIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d snowed leaves textures", snowedLeavesIcons.numLoaded));
    }

}
