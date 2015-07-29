package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.CubeQuadrantQuads;
import mods.betterfoliage.client.render.impl.primitives.IQuadCollection;
import mods.betterfoliage.client.render.impl.primitives.OctaPrismQuadrantQuads;
import mods.betterfoliage.client.texture.LogTextures.LogInfo;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLogs extends BFAbstractRenderer {

	public RenderBlockLogs() {
		isStandardRenderBlocked = true;
	}
	
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	boolean result = Config.logsEnabled && Config.logs.matchesID(blockState.getBlock());
    	
    	// fallback to regular rendering if not a proper log
    	LogInfo logInfo = BetterFoliageClient.logRegistry.logInfoMap.get(blockState);
    	result &= (logInfo != null && logInfo.sideTexture != null && logInfo.endTexture != null);
    	
    	return result;
    }

    @Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
    	if (layer != EnumWorldBlockLayer.SOLID) return false;
    	
    	// set axes
    	LogInfo logInfo = BetterFoliageClient.logRegistry.logInfoMap.get(blockState);
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
        
        BlockShadingData shading = shadingData.get();
        shading.update(blockAccess, blockState.getBlock(), pos, useAO);
    	IQuadCollection quadsNN = getQuadrant(blockPos, logHorzDir1, logHorzDir2, logVertDir, 3, !topBlocked, !bottomBlocked,
    			connectNN || connectNP || connectPN ? null : (connectPP ? Config.logsLargeRadius : Config.logsSmallRadius), logInfo.sideTexture, logInfo.endTexture);
    	IQuadCollection quadsPN = getQuadrant(blockPos.add(logHorzDir1), logHorzDir2, logHorzDir1.getOpposite(), logVertDir, 2, !topBlocked, !bottomBlocked,
    			connectPN || connectNN || connectPP ? null : (connectNP ? Config.logsLargeRadius : Config.logsSmallRadius), logInfo.sideTexture, logInfo.endTexture);
    	IQuadCollection quadsPP = getQuadrant(blockPos.add(logHorzDir1).add(logHorzDir2), logHorzDir1.getOpposite(), logHorzDir2.getOpposite(), logVertDir, 1, !topBlocked, !bottomBlocked,
    			connectPP || connectNP || connectPN ? null : (connectNN ? Config.logsLargeRadius : Config.logsSmallRadius), logInfo.sideTexture, logInfo.endTexture);
    	IQuadCollection quadsNP = getQuadrant(blockPos.add(logHorzDir2), logHorzDir2.getOpposite(), logHorzDir1, logVertDir, 0, !topBlocked, !bottomBlocked,
    			connectNP || connectNN || connectPP ? null : (connectPN ? Config.logsLargeRadius : Config.logsSmallRadius), logInfo.sideTexture, logInfo.endTexture);
    	
    	quadsNN.setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
    	quadsPN.setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
    	quadsPP.setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
    	quadsNP.setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
    	return true;
    }
    
    protected IQuadCollection getQuadrant(Double3 originCorner, EnumFacing horz1Dir, EnumFacing horz2Dir, EnumFacing vertDir, int uvRot, boolean hasTop, boolean hasBottom, Double chamferSize, TextureAtlasSprite sideTexture, TextureAtlasSprite endTexture) {
    	if (chamferSize == null || chamferSize < 0.01) {
    		return CubeQuadrantQuads.create(originCorner, horz1Dir, horz2Dir, vertDir, uvRot, hasTop, hasBottom).setTexture(sideTexture, endTexture);
    	} else {
    		return OctaPrismQuadrantQuads.create(originCorner, horz1Dir, horz2Dir, vertDir, chamferSize, uvRot, hasTop, hasBottom).setTexture(sideTexture, endTexture);
    	}
    }
    
    protected boolean isBlocked(IBlockAccess blockAccess, BlockPos pos, EnumFacing... offsets) {
    	BlockPos offsetPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    	for (EnumFacing offset : offsets) offsetPos = offsetPos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());
    	Block block = blockAccess.getBlockState(offsetPos).getBlock();
    	return block.isOpaqueCube() && !Config.logs.matchesID(block);
    }
    
    protected boolean isConnected(IBlockAccess blockAccess, BlockPos pos, EnumFacing referenceDir, boolean matchOrientation, EnumFacing... offsets) {
    	BlockPos offsetPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
    	for (EnumFacing offset : offsets) offsetPos = offsetPos.add(offset.getFrontOffsetX(), offset.getFrontOffsetY(), offset.getFrontOffsetZ());
    	Block block = blockAccess.getBlockState(pos).getBlock();
    	if (!Config.logs.matchesID(block)) return false;
    	LogInfo logInfo = BetterFoliageClient.logRegistry.logInfoMap.get(blockAccess.getBlockState(offsetPos));
    	return logInfo != null && (logInfo.verticalDir!= referenceDir ^ matchOrientation);
    }
}
