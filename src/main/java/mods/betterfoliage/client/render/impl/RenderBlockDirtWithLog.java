package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.OffsetBlockAccess;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class RenderBlockDirtWithLog extends BFAbstractRenderer {

	public static final EnumFacing[] sides = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
	
	public RenderBlockDirtWithLog() {
		isStandardRenderBlocked = true;
	}
	
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	if (!(Config.logsEnabled && Config.logsConnectGrass)) return false;
        
        if (!Config.dirt.matchesID(blockState.getBlock())) return false;
        if (!Config.logs.matchesID(blockAccess.getBlockState(pos.up()).getBlock())) return false;
        
        return true;
    }
    
    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	for (EnumFacing side : sides) {
    		IBlockState state = blockAccess.getBlockState(pos.offset(side));
    		if (Config.grass.matchesID(state.getBlock()) && state.getBlock().canRenderInLayer(layer)) {
	        	IBlockAccess offsetBlockAccess = new OffsetBlockAccess(blockAccess, pos, side);
	        	IBakedModel model = dispatcher.getModelFromBlockState(state, offsetBlockAccess, pos);
	        	return renderModel(offsetBlockAccess, model, state, pos, worldRenderer);
    		}
    	}
    	return dispatcher.renderBlock(blockState, pos, blockAccess, worldRenderer);
    }
    
}
