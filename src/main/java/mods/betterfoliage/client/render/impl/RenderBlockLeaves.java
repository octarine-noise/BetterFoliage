package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.IShadingData;
import mods.betterfoliage.client.render.RenderCache;
import mods.betterfoliage.client.render.Rotation;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.BlockCrossedQuads;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.IQuadCollection;
import mods.betterfoliage.client.texture.LeafTextures.LeafInfo;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLeaves extends BFAbstractRenderer {

	public static Rotation[] rotations = new Rotation[] { Rotation.identity, Rotation.rotatePositive[0], Rotation.rotatePositive[2] };
	
	public RenderCache<BlockCrossedQuads> translatedLeaves = new RenderCache<BlockCrossedQuads>(shadingData, rotations) {
		@Override
		public BlockCrossedQuads drawQuads(int variation) {
			return BlockCrossedQuads.create(null, null, 0.5 * Config.leavesSize);
		}
	};
	
	public RenderCache<BlockCrossedQuads> skewedLeaves = new RenderCache<BlockCrossedQuads>(shadingData, rotations) {
		@Override
		public BlockCrossedQuads drawQuads(int variation) {
			return BlockCrossedQuads.create(
				random.getCylinderXZY(Config.leavesHOffset, Config.leavesVOffset, variation),
				random.getCylinderXZY(Config.leavesHOffset, Config.leavesVOffset, variation + 1),
				0.5 * Config.leavesSize);
		}
	};
	
    @Override
	public void onConfigReload() {
    	skewedLeaves.reinit();
    	translatedLeaves.reinit();
	}

	public TextureSet snowedLeavesIcons = new TextureSet("bettergrassandleaves", "blocks/better_leaves_snowed_%d");
    
	public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	return Config.leavesEnabled && Config.leaves.matchesID(blockState.getBlock());
    }

    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
    	if (isBlockObscured(blockAccess, pos)) return false;
    	
        // check if we have leaf textures for this block
        LeafInfo leafInfo = BetterFoliageClient.leafRegistry.leafInfoMap.get(blockState);
        if (leafInfo == null || leafInfo.roundLeafTexture == null) {
            return false;
        }

        // check conditions
        boolean isSnowTop = blockAccess.getBlockState(pos.up()).getBlock().getMaterial() == Material.snow;
        
        // render round leaves
        int offsetVariation = getSemiRandomFromPos(pos, 0);
        int uvVariation = getSemiRandomFromPos(pos, 1);
        Color4 blockColor = Color4.fromARGB(blockState.getBlock().colorMultiplier(blockAccess, pos, 0)).opaque().multiply(0.75f);
        Double3 blockCenter = new Double3(pos).add(0.5, 0.5, 0.5);
        if (!Config.leavesSkew) blockCenter.add(random.getCylinderXZY(Config.leavesHOffset, Config.leavesVOffset, offsetVariation));

        ShadersModIntegration.startLeavesQuads(worldRenderer);
    	shadingData.get().update(blockAccess, blockState.getBlock(), pos, useAO);
        
        for (int idx = 0; idx < (Config.leavesDense ? 3 : 1); idx++) {
        	RenderCache<?> cache = Config.leavesSkew ? skewedLeaves : translatedLeaves;
        	IShadingData shading = cache.getShading(idx);
        	IQuadCollection roundLeaves = cache.getQuads(idx, offsetVariation);
        	// synchronize on the model to avoid problems if both rendering threads happen to use the exact same model at the same time
        	// cost of synchronization is very low if there is no blocking (and it should be very infrequent)
        	synchronized (roundLeaves) {
            	roundLeaves.setTexture(leafInfo.roundLeafTexture, uvVariation).setBrightness(shading).setColor(shading, blockColor).render(worldRenderer, blockCenter);
            	if (isSnowTop && idx == 0) roundLeaves.setTexture(leafInfo.roundLeafTexture, uvVariation).setColor(shading, blockColor).render(worldRenderer, blockCenter);
			}
        }
        ShadersModIntegration.finish(worldRenderer);
        
        return true;
    }
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        snowedLeavesIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d snowed leaves textures", snowedLeavesIcons.numLoaded));
    }

}
