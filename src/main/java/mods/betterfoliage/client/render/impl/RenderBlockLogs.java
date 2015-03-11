package mods.betterfoliage.client.render.impl;

import mods.betterfoliage.client.render.FakeRenderBlockAOBase;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import mods.betterfoliage.common.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlockLogs extends FakeRenderBlockAOBase implements IRenderBlockDecorator {
	
    /** Quick lookup array to get AO values of a given block corner */
    protected ShadingValues[][][] shadingLookup = new ShadingValues[6][6][6];
        
	public boolean isBlockAccepted(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		return Config.logsEnabled && Config.logs.matchesID(block);
	}
	
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		blockAccess = world;
		ForgeDirection logVertDir = getLogVerticalDir(blockAccess, x, y, z);

		// use default renderer if we cannot determine log orientation 
		if (logVertDir == ForgeDirection.UNKNOWN) {
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
            return true;
        }
		
		// get AO data, bail if rendering block breaking
		renderWorldBlockBase(1, world, x, y, z, block, modelId, renderer);
		if (renderer.hasOverrideBlockTexture()) return true;

		// set axes
		Double3 blockPos = new Double3(x, y, z);
		ForgeDirection logHorzDir1, logHorzDir2;
		if (logVertDir == ForgeDirection.UP || logVertDir == ForgeDirection.DOWN) {
		    logVertDir = ForgeDirection.UP;
		    logHorzDir1 = ForgeDirection.EAST;
		    logHorzDir2 = ForgeDirection.SOUTH;
		} else if (logVertDir == ForgeDirection.EAST || logVertDir == ForgeDirection.WEST) {
		    logVertDir = ForgeDirection.EAST;
		    logHorzDir1 = ForgeDirection.SOUTH;
		    logHorzDir2 = ForgeDirection.UP;
		} else {
		    logVertDir = ForgeDirection.SOUTH;
	        logHorzDir1 = ForgeDirection.UP;
	        logHorzDir2 = ForgeDirection.EAST;
		}
		
	    // check neighborhood
        boolean connectP1 = Config.logsConnect && isConnected(world, x, y, z, logVertDir, true, logHorzDir1);
        boolean connectP2 = Config.logsConnect && isConnected(world, x, y, z, logVertDir, true, logHorzDir2);
        boolean connectN1 = Config.logsConnect && isConnected(world, x, y, z, logVertDir, true, logHorzDir1.getOpposite());
        boolean connectN2 = Config.logsConnect && isConnected(world, x, y, z, logVertDir, true, logHorzDir2.getOpposite());
        
        boolean connectPP = connectP1 && connectP2 && isConnected(world, x, y, z, logVertDir, true, logHorzDir1, logHorzDir2);
        boolean connectPN = connectP1 && connectN2 && isConnected(world, x, y, z, logVertDir, true, logHorzDir1, logHorzDir2.getOpposite());
        boolean connectNP = connectN1 && connectP2 && isConnected(world, x, y, z, logVertDir, true, logHorzDir1.getOpposite(), logHorzDir2);
        boolean connectNN = connectN1 && connectN2 && isConnected(world, x, y, z, logVertDir, true, logHorzDir1.getOpposite(), logHorzDir2.getOpposite());
        
        boolean topBlocked = isBlocked(world, x, y, z, logVertDir);
        boolean bottomBlocked = isBlocked(world, x, y, z, logVertDir.getOpposite());
        
		// get icons
		IIcon iconTop = topBlocked ? null : RenderUtils.getIcon(world, block, x, y, z,  logVertDir);
		IIcon iconBottom = bottomBlocked ? null : RenderUtils.getIcon(world, block, x, y, z, logVertDir);
		IIcon iconP1 = RenderUtils.getIcon(world, block, x, y, z,  logHorzDir1);
		IIcon iconN1 = RenderUtils.getIcon(world, block, x, y, z,  logHorzDir1.getOpposite());
        IIcon iconP2 = RenderUtils.getIcon(world, block, x, y, z,  logHorzDir2);
        IIcon iconN2 = RenderUtils.getIcon(world, block, x, y, z,  logHorzDir2.getOpposite());
        
        // draw log
		drawQuarterLog(blockPos,logHorzDir1, logHorzDir2, logVertDir,
		               connectNN || connectNP || connectPN ? null : (connectPP ? Config.logsLargeRadius : Config.logsSmallRadius),
		               iconN2, iconN1, iconTop, iconBottom, 0);
		drawQuarterLog(blockPos.add(new Double3(logHorzDir1)), logHorzDir2, logHorzDir1.getOpposite(), logVertDir,
		               connectPN || connectNN || connectPP ? null : (connectNP ? Config.logsLargeRadius : Config.logsSmallRadius),
		               iconP1, iconN2, iconTop, iconBottom, 3);
		drawQuarterLog(blockPos.add(new Double3(logHorzDir1)).add(new Double3(logHorzDir2)), logHorzDir1.getOpposite(), logHorzDir2.getOpposite(), logVertDir,
		               connectPP || connectNP || connectPN ? null : (connectNN ? Config.logsLargeRadius : Config.logsSmallRadius),
		               iconP2, iconP1, iconTop, iconBottom, 2);
		drawQuarterLog(blockPos.add(new Double3(logHorzDir2)), logHorzDir2.getOpposite(), logHorzDir1, logVertDir,
		               connectNP || connectNN || connectPP ? null : (connectPN ? Config.logsLargeRadius : Config.logsSmallRadius),
		               iconN1, iconP2, iconTop, iconBottom, 1);
		return true;
	}

    protected ForgeDirection getLogVerticalDir(IBlockAccess blockAccess, int x, int y, int z) {
    	// standard way as done by BlockRotatedPillar
        switch((blockAccess.getBlockMetadata(x, y, z) >> 2) & 3) {
            case 0: return ForgeDirection.UP;
            case 1: return ForgeDirection.EAST;
            case 2: return ForgeDirection.SOUTH;
        }
        return ForgeDirection.UNKNOWN;
    }
    
	/** Draw one corner of the log
	 * @param origin corner vertex of block
	 * @param horz1Dir one of the log-perpendicular directions
	 * @param horz2Dir one of the log-perpendicular directions
	 * @param vertDir log-parallel direction
	 * @param chamferSize chamfer amount for corner. Value <= 0.01 of null means no chamfering
	 * @param textureSide1 texture of side parallel to horz1Dir
	 * @param textureSide2 texture of side parallel to horz2Dir
	 * @param textureTop texture of top lid (+vertDir)
	 * @param textureBottom texture of bottom lid (-vertDir)
	 * @param uvRot number of 90deg rotations for top & bottom lid textures
	 */
	protected void drawQuarterLog(Double3 origin, ForgeDirection horz1Dir, ForgeDirection horz2Dir, ForgeDirection vertDir, Double chamferSize, IIcon textureSide1, IIcon textureSide2, IIcon textureTop, IIcon textureBottom, int uvRot) {
	    double[] vSide = new double[] {16.0, 16.0, 0.0, 0.0};
	    double[] uTop = new double[] {uValues[uvRot & 3], uValues[(uvRot + 1) & 3], uValues[(uvRot + 2) & 3], uValues[(uvRot + 3) & 3]};
	    double[] vTop = new double[] {vValues[uvRot & 3], vValues[(uvRot + 1) & 3], vValues[(uvRot + 2) & 3], vValues[(uvRot + 3) & 3]};
	    
	    Double3 horz1 = new Double3(horz1Dir);
	    Double3 horz2 = new Double3(horz2Dir);
	    Double3 vert = new Double3(vertDir);
	    
        ShadingValues aoFace1FarBottom = null, aoFace1NearBottom = null, aoFace1FarTop = null, aoFace1NearTop = null;
        ShadingValues aoFace2FarBottom = null, aoFace2NearBottom = null, aoFace2FarTop = null, aoFace2NearTop = null;
        ShadingValues aoTopOrigin = null, aoTop1 = null, aoTop2 = null;
        ShadingValues aoBottomOrigin = null, aoBottom1 = null, aoBottom2 = null;
        ShadingValues aoTopCenter = null, aoBottomCenter = null;
        
        if (Minecraft.isAmbientOcclusionEnabled()) {
            aoFace1FarBottom = getAoLookup(horz2Dir.getOpposite(), horz1Dir, vertDir.getOpposite());
            aoFace1NearBottom = getAoLookup(horz2Dir.getOpposite(), horz1Dir.getOpposite(), vertDir.getOpposite());
            aoFace1FarTop = getAoLookup(horz2Dir.getOpposite(), horz1Dir, vertDir);
            aoFace1NearTop = getAoLookup(horz2Dir.getOpposite(), horz1Dir.getOpposite(), vertDir);
            
            aoFace2FarBottom = getAoLookup(horz1Dir.getOpposite(), horz2Dir, vertDir.getOpposite());
            aoFace2NearBottom = getAoLookup(horz1Dir.getOpposite(), horz2Dir.getOpposite(), vertDir.getOpposite());
            aoFace2FarTop = getAoLookup(horz1Dir.getOpposite(), horz2Dir, vertDir);
            aoFace2NearTop = getAoLookup(horz1Dir.getOpposite(), horz2Dir.getOpposite(), vertDir);
            
            aoTopOrigin = getAoLookup(vertDir, horz1Dir.getOpposite(), horz2Dir.getOpposite());
            aoTop1 = getAoLookup(vertDir, horz1Dir, horz2Dir.getOpposite());
            aoTop2 = getAoLookup(vertDir, horz1Dir.getOpposite(), horz2Dir);
            
            aoBottomOrigin = getAoLookup(vertDir.getOpposite(), horz1Dir.getOpposite(), horz2Dir.getOpposite());
            aoBottom1 = getAoLookup(vertDir.getOpposite(), horz1Dir, horz2Dir.getOpposite());
            aoBottom2 = getAoLookup(vertDir.getOpposite(), horz1Dir.getOpposite(), horz2Dir);
            
            aoTopCenter = avgShadingForFace(vertDir);
            aoBottomCenter = avgShadingForFace(vertDir.getOpposite());
        }
        
	    if (chamferSize != null && chamferSize > 0.01) {
	        // chamfered
            Double3 mid1 = origin.add(horz1.scale(0.5));
            Double3 cham1 = origin.add(horz1.scale(chamferSize));
            Double3 mid2 = origin.add(horz2.scale(0.5));
            Double3 cham2 = origin.add(horz2.scale(chamferSize));
            Double3 chamCenter = cham1.add(cham2).scale(0.5);
            Double3 blockCenter = origin.add(horz1.scale(0.5)).add(horz2.scale(0.5));
            
            double uLeft = chamferSize * 16.0;
            double uRight = 16.0 - uLeft;
    	    
            // face 1 middle
            renderQuad(textureSide1, mid1, cham1, cham1.add(vert), mid1.add(vert), new double[]{8.0, uRight, uRight, 8.0}, vSide,
                                  avgShading(aoFace1FarBottom, aoFace1NearBottom), aoFace1NearBottom, aoFace1NearTop, avgShading(aoFace1FarTop, aoFace1NearTop));
            // face 1 chamfer
            renderQuad(textureSide1, cham1, chamCenter, chamCenter.add(vert), cham1.add(vert), new double[]{uRight, 16.0, 16.0, uRight}, vSide,
                                  aoFace1NearBottom, avgShading(aoFace1NearBottom, aoFace2NearBottom), avgShading(aoFace1NearTop, aoFace2NearTop), aoFace1NearTop);
            // face 2 chamfer
            renderQuad(textureSide2, chamCenter, cham2, cham2.add(vert), chamCenter.add(vert), new double[]{0.0, uLeft, uLeft, 0.0}, vSide,
                                  avgShading(aoFace1NearBottom, aoFace2NearBottom), aoFace2NearBottom, aoFace2NearTop, avgShading(aoFace1NearTop, aoFace2NearTop));
            // face 2 middle
            renderQuad(textureSide2, cham2, mid2, mid2.add(vert), cham2.add(vert), new double[]{uLeft, 8.0, 8.0, uLeft}, vSide,
                                  aoFace2NearBottom, avgShading(aoFace2FarBottom, aoFace2NearBottom), avgShading(aoFace2FarTop, aoFace2NearTop), aoFace2NearTop);
            
            if (textureTop != null) {
                // top lid 1
                renderQuad(textureTop, blockCenter.add(vert), mid1.add(vert), cham1.add(vert), chamCenter.add(vert),
                                      new double[]{8.0, (uTop[0] + uTop[3]) * 0.5, uTop[0] * (1 - chamferSize) + uTop[3] * chamferSize, uTop[0]},
                                      new double[]{8.0, (vTop[0] + vTop[3]) * 0.5, vTop[0] * (1 - chamferSize) + vTop[3] * chamferSize, vTop[0]},
                                      aoTopCenter, avgShading(aoTop1, aoTopOrigin), aoTopOrigin, aoTopOrigin);
                // top lid 2
                renderQuad(textureTop, cham2.add(vert), mid2.add(vert), blockCenter.add(vert), chamCenter.add(vert),
                                      new double[]{uTop[0] * (1 - chamferSize) + uTop[1] * chamferSize, (uTop[0] + uTop[1]) * 0.5, 8.0, uTop[0]},
                                      new double[]{vTop[0] * (1 - chamferSize) + vTop[1] * chamferSize, (vTop[0] + vTop[1]) * 0.5, 8.0, vTop[0]},
                                      aoTopOrigin, avgShading(aoTop2, aoTopOrigin), aoTopCenter, aoTopOrigin);
            }
            
            if (textureBottom != null) {
                // bottom lid 1
                renderQuad(textureBottom, blockCenter, chamCenter, cham1, mid1,
                                      new double[]{8.0, uTop[0], uTop[0] * (1 - chamferSize) + uTop[3] * chamferSize, (uTop[0] + uTop[3]) * 0.5},
                                      new double[]{8.0, vTop[0], vTop[0] * (1 - chamferSize) + vTop[3] * chamferSize, (vTop[0] + vTop[3]) * 0.5},
                                      aoBottomCenter, aoBottomOrigin, aoBottomOrigin, avgShading(aoBottom1, aoBottomOrigin));
                // bottom lid 2
                renderQuad(textureBottom, cham2, chamCenter, blockCenter, mid2,
                                      new double[]{uTop[0] * (1 - chamferSize) + uTop[1] * chamferSize, uTop[0], 8.0, (uTop[0] + uTop[1]) * 0.5},
                                      new double[]{vTop[0] * (1 - chamferSize) + vTop[1] * chamferSize, vTop[0], 8.0, (vTop[0] + vTop[1]) * 0.5},
                                      aoBottomOrigin, aoBottomOrigin, aoBottomCenter, avgShading(aoBottom2, aoBottomOrigin));
            }
	    } else {
	        // normal
	        Double3 mid1 = origin.add(horz1.scale(0.5));
	        Double3 mid2 = origin.add(horz2.scale(0.5));
	        Double3 blockCenter = origin.add(horz1.scale(0.5)).add(horz2.scale(0.5));
	        
	        // face 1
	        renderQuad(textureSide1, mid1, origin, origin.add(vert), mid1.add(vert), new double[]{8.0, 16.0, 16.0, 8.0}, vSide,
	                   avgShading(aoFace1FarBottom, aoFace1NearBottom), aoFace1NearBottom, aoFace1NearTop, avgShading(aoFace1FarTop, aoFace1NearTop));
	        
	        // face 2
            renderQuad(textureSide2, origin, mid2, mid2.add(vert), origin.add(vert), new double[]{0.0, 8.0, 8.0, 0.0}, vSide,
                       aoFace2NearBottom, avgShading(aoFace2FarBottom, aoFace2NearBottom), avgShading(aoFace2FarTop, aoFace2NearTop), aoFace2NearTop);
            
            // top lid
            if (textureTop != null) renderQuad(textureTop, origin.add(vert), mid2.add(vert), blockCenter.add(vert), mid1.add(vert),
                                               new double[]{uTop[0], (uTop[0] + uTop[1]) * 0.5, 8.0, (uTop[0] + uTop[3]) * 0.5},
                                               new double[]{vTop[0], (vTop[0] + vTop[1]) * 0.5, 8.0, (vTop[0] + vTop[3]) * 0.5},
                                               aoTopOrigin, avgShading(aoTop2, aoTopOrigin), aoTopCenter, avgShading(aoTop1, aoTopOrigin));
            
            // bottom lid
            if (textureBottom != null) renderQuad(textureBottom, mid1, blockCenter , mid2, origin,
                                                  new double[]{(uTop[0] + uTop[3]) * 0.5, 8.0, (uTop[0] + uTop[1]) * 0.5, uTop[0]},
                                                  new double[]{(vTop[0] + vTop[3]) * 0.5, 8.0, (vTop[0] + vTop[1]) * 0.5, vTop[0]},
                                                  avgShading(aoBottom1, aoBottomOrigin), aoBottomCenter, avgShading(aoBottom2, aoBottomOrigin), aoBottomOrigin);
	    }
	}
	
	/** Get the average shading values of 2 AO data points
	 * @param shading1 data 1
	 * @param shading2 data 2
	 * @return average
	 */
	protected ShadingValues avgShading(ShadingValues shading1, ShadingValues shading2) {
	    if (shading1 == null || shading2 == null) return null;
	    ShadingValues result = new ShadingValues();
	    result.brightness = (shading1.brightness + shading2.brightness) / 2;
	    result.red = (shading1.red + shading2.red) * 0.5f;
	    result.green = (shading1.green + shading2.green) * 0.5f;
	    result.blue = (shading1.blue + shading2.blue) * 0.5f;
	    return result;
	}
	
	/** Get the average shading values of the 4 AO data points of a face
	 * @param dir face direction
	 * @return average
	 */
	protected ShadingValues avgShadingForFace(ForgeDirection dir) {
	    setShadingForFace(dir);
	    ShadingValues result = new ShadingValues();
	    result.brightness = (faceAONN.brightness + faceAONP.brightness + faceAOPN.brightness + faceAOPP.brightness) / 4;
	    result.red = (faceAONN.red + faceAONP.red + faceAOPN.red + faceAOPP.red) * 0.25f;
	    result.green = (faceAONN.green + faceAONP.green + faceAOPN.green + faceAOPP.green) * 0.25f;
	    result.blue = (faceAONN.blue + faceAONP.blue + faceAOPN.blue + faceAOPP.blue) * 0.25f;
	    return result;
	}
	
	/** Determine if a log block is connected to another block
	 * @param blockAccess world object
	 * @param x
	 * @param y
	 * @param z
	 * @param referenceDir orientation of log block being rendered
	 * @param offsets offset given coordinate by 1 along these directions
	 * @return true if the log connects to this block
	 */
	protected boolean isConnected(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection referenceDir, boolean matchOrientation, ForgeDirection... offsets) {
	    int xOff = x;
	    int yOff = y;
	    int zOff = z;
	    for (ForgeDirection dir : offsets) {
	        xOff += dir.offsetX;
	        yOff += dir.offsetY;
	        zOff += dir.offsetZ;
	    }
	    return Config.logs.matchesID(blockAccess.getBlock(xOff, yOff, zOff)) && (getLogVerticalDir(blockAccess, xOff, yOff, zOff) != referenceDir ^ matchOrientation); 
    }

    /** Determines if the block is fully obscured from a given side
     * @param blockAccess
     * @param x
     * @param y
     * @param z
     * @param offsets offset given coordinate by 1 along these directions
     * @return
     */
    protected boolean isBlocked(IBlockAccess blockAccess, int x, int y, int z, ForgeDirection... offsets) {
        int xOff = x;
        int yOff = y;
        int zOff = z;
        for (ForgeDirection dir : offsets) {
            xOff += dir.offsetX;
            yOff += dir.offsetY;
            zOff += dir.offsetZ;
        }
        Block block = blockAccess.getBlock(xOff, yOff, zOff);
        return block.isOpaqueCube() && !Config.logs.matchesID(block);
    }
	
    public RenderBlockLogs() {
    	vValues = new double[] {16.0, 16.0, 0.0, 0.0};
    	
        putLookup(ForgeDirection.DOWN, ForgeDirection.SOUTH, ForgeDirection.EAST, aoYNXZPP);
        putLookup(ForgeDirection.DOWN, ForgeDirection.SOUTH, ForgeDirection.WEST, aoYNXZNP);
        putLookup(ForgeDirection.DOWN, ForgeDirection.NORTH, ForgeDirection.EAST, aoYNXZPN);
        putLookup(ForgeDirection.DOWN, ForgeDirection.NORTH, ForgeDirection.WEST, aoYNXZNN);
        
        putLookup(ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.EAST, aoYPXZPP);
        putLookup(ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.WEST, aoYPXZNP);
        putLookup(ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.EAST, aoYPXZPN);
        putLookup(ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.WEST, aoYPXZNN);
        
        putLookup(ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.NORTH, aoXNYZPN);
        putLookup(ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.SOUTH, aoXNYZPP);
        putLookup(ForgeDirection.WEST, ForgeDirection.DOWN, ForgeDirection.NORTH, aoXNYZNN);
        putLookup(ForgeDirection.WEST, ForgeDirection.DOWN, ForgeDirection.SOUTH, aoXNYZNP);
        
        putLookup(ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.NORTH, aoXPYZPN);
        putLookup(ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH, aoXPYZPP);
        putLookup(ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.NORTH, aoXPYZNN);
        putLookup(ForgeDirection.EAST, ForgeDirection.DOWN, ForgeDirection.SOUTH, aoXPYZNP);
        
        putLookup(ForgeDirection.NORTH, ForgeDirection.UP, ForgeDirection.EAST, aoZNXYPP);
        putLookup(ForgeDirection.NORTH, ForgeDirection.UP, ForgeDirection.WEST, aoZNXYNP);
        putLookup(ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.EAST, aoZNXYPN);
        putLookup(ForgeDirection.NORTH, ForgeDirection.DOWN, ForgeDirection.WEST, aoZNXYNN);
        
        putLookup(ForgeDirection.SOUTH, ForgeDirection.UP, ForgeDirection.EAST, aoZPXYPP);
        putLookup(ForgeDirection.SOUTH, ForgeDirection.UP, ForgeDirection.WEST, aoZPXYNP);
        putLookup(ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.EAST, aoZPXYPN);
        putLookup(ForgeDirection.SOUTH, ForgeDirection.DOWN, ForgeDirection.WEST, aoZPXYNN);
    }
    
    protected void putLookup(ForgeDirection face, ForgeDirection dir1, ForgeDirection dir2, ShadingValues shading) {
        shadingLookup[face.ordinal()][dir1.ordinal()][dir2.ordinal()] = shading;
        shadingLookup[face.ordinal()][dir2.ordinal()][dir1.ordinal()] = shading;
    }
    
    protected ShadingValues getAoLookup(ForgeDirection face, ForgeDirection dir1, ForgeDirection dir2) {
        return shadingLookup[face.ordinal()][dir1.ordinal()][dir2.ordinal()];
    }
}
