package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.OctaPrismQuadrantQuads;
import mods.betterfoliage.client.texture.LogTextures.LogInfo;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLogs extends BFAbstractRenderer {

	public RenderBlockLogs() {
		shadingData.shadingBitSet.clear(0);
	}
	
    @Override
    public boolean renderFeatureForBlock(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO) {
    	if (!Config.logsEnabled) return false;
    	if (!Config.logs.matchesID(blockState.getBlock())) return false;
    	
    	LogInfo logInfo = BetterFoliageClient.logRegistry.logInfoMap.get(blockState);
    	if (logInfo == null || logInfo.sideTexture == null || logInfo.endTexture == null) return false;
    	
    	// set axes
    	Double3 blockPos = new Double3(pos);
    	EnumFacing logVertDir = logInfo.verticalDir;
    	EnumFacing logHorzDir1, logHorzDir2;
		if (logVertDir == EnumFacing.UP || logVertDir == EnumFacing.DOWN) {
		    logVertDir = EnumFacing.UP;
		    logHorzDir1 = EnumFacing.EAST;
		    logHorzDir2 = EnumFacing.SOUTH;
		} else if (logVertDir == EnumFacing.EAST || logVertDir == EnumFacing.WEST) {
		    logVertDir = EnumFacing.EAST;
		    logHorzDir1 = EnumFacing.SOUTH;
		    logHorzDir2 = EnumFacing.UP;
		} else {
		    logVertDir = EnumFacing.SOUTH;
	        logHorzDir1 = EnumFacing.UP;
	        logHorzDir2 = EnumFacing.EAST;
		}
    	
	    // check neighborhood
        boolean connectP1 = Config.logsConnect && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1);
        boolean connectP2 = Config.logsConnect && isConnected(blockAccess, pos, logVertDir, true, logHorzDir2);
        boolean connectN1 = Config.logsConnect && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1.getOpposite());
        boolean connectN2 = Config.logsConnect && isConnected(blockAccess, pos, logVertDir, true, logHorzDir2.getOpposite());
        
        boolean connectPP = connectP1 && connectP2 && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1, logHorzDir2);
        boolean connectPN = connectP1 && connectN2 && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1, logHorzDir2.getOpposite());
        boolean connectNP = connectN1 && connectP2 && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1.getOpposite(), logHorzDir2);
        boolean connectNN = connectN1 && connectN2 && isConnected(blockAccess, pos, logVertDir, true, logHorzDir1.getOpposite(), logHorzDir2.getOpposite());
        
        boolean topBlocked = isBlocked(blockAccess, pos, logVertDir);
        boolean bottomBlocked = isBlocked(blockAccess, pos, logVertDir.getOpposite());
        
        double chamferSize = 0.25;
    	shadingData.update(blockAccess, blockState.getBlock(), pos, useAO);
    	OctaPrismQuadrantQuads quadsNN = OctaPrismQuadrantQuads.create(blockPos, logHorzDir1, logHorzDir2, logVertDir, chamferSize, 3, !topBlocked, !bottomBlocked);
    	OctaPrismQuadrantQuads quadsPN = OctaPrismQuadrantQuads.create(blockPos.add(logHorzDir1), logHorzDir2, logHorzDir1.getOpposite(), logVertDir, chamferSize, 2, !topBlocked, !bottomBlocked);
    	OctaPrismQuadrantQuads quadsPP = OctaPrismQuadrantQuads.create(blockPos.add(logHorzDir1).add(logHorzDir2), logHorzDir1.getOpposite(), logHorzDir2.getOpposite(), logVertDir, chamferSize, 1, !topBlocked, !bottomBlocked);
    	OctaPrismQuadrantQuads quadsNP = OctaPrismQuadrantQuads.create(blockPos.add(logHorzDir2), logHorzDir2.getOpposite(), logHorzDir1, logVertDir, chamferSize, 0, !topBlocked, !bottomBlocked);
    	
    	quadsNN.setTexture(logInfo.sideTexture, logInfo.endTexture).setBrightness(shadingData).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
    	quadsPN.setTexture(logInfo.sideTexture, logInfo.endTexture).setBrightness(shadingData).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
    	quadsPP.setTexture(logInfo.sideTexture, logInfo.endTexture).setBrightness(shadingData).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
    	quadsNP.setTexture(logInfo.sideTexture, logInfo.endTexture).setBrightness(shadingData).setColor(shadingData, Color4.opaqueWhite).render(worldRenderer);
    	return true;
    }
    
    protected EnumFacing getLogVerticalDir(IBlockAccess blockAccess, BlockPos pos) {
    	IBlockState state = blockAccess.getBlockState(pos);
    	BlockLog.EnumAxis axis = (EnumAxis) state.getValue(BlockLog.LOG_AXIS);
    	switch (axis) {
			case X: return EnumFacing.EAST;
			case Z: return EnumFacing.SOUTH;
			default: return EnumFacing.UP;
		}
    }
    
    protected boolean isBlocked(IBlockAccess blockAccess, BlockPos pos, EnumFacing... offsets) {
    	BlockPos offsetPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    	for (EnumFacing offset : offsets) offsetPos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());
    	Block block = blockAccess.getBlockState(pos).getBlock();
    	return block.isOpaqueCube() && !Config.logs.matchesID(block);
    }
    
    protected boolean isConnected(IBlockAccess blockAccess, BlockPos pos, EnumFacing referenceDir, boolean matchOrientation, EnumFacing... offsets) {
    	BlockPos offsetPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    	for (EnumFacing offset : offsets) offsetPos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());
    	Block block = blockAccess.getBlockState(pos).getBlock();
    	return block.isOpaqueCube() && !Config.logs.matchesID(block);
    }
}
