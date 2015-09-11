package mods.betterfoliage.client.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.integration.CLCIntegration;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.util.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Block renderer base class. Stores calculated ambient occlusion light and color values when rendering
 *  block sides for later use.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class RenderBlockAOBase extends RenderBlocks {

	/** AO light and color values
	 * @author octarine-noise
	 */
	@SideOnly(Side.CLIENT)
	public static class ShadingValues {
		public int passCounter = 0;
		public int brightness;
		public float red;
		public float green;
		public float blue;
		
		public void setGray(float value) {
			red = value; green = value; blue = value;
		}
		
		public void setColor(int value) {
			red = ((value >> 16) & 0xFF) / 256.0f;
			green = ((value >> 8) & 0xFF) / 256.0f;
			blue = (value & 0xFF) / 256.0f;
		}
		
		@Override
		public String toString() {
			return String.format("Br(%d, %d, %d), Col(%.2f, %.2f, %.2f)", (brightness >> 16) & 0xFF, (brightness >> 8) & 0xFF, (brightness & 0xFF), red, green, blue);
		}
	}
	
	protected int drawPass = 0;
	
	protected boolean skipFaces = false;
	
	protected double[] uValues = new double[] {0.0, 16.0, 16.0, 0.0};
	protected double[] vValues = new double[] {0.0, 0.0, 16.0, 16.0};
	
	protected ForgeDirection[] faceDir1 = new ForgeDirection[] {ForgeDirection.WEST, ForgeDirection.WEST, ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.NORTH};
	protected ForgeDirection[] faceDir2 = new ForgeDirection[] {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.UP, ForgeDirection.UP, ForgeDirection.UP, ForgeDirection.UP};
	
	/** Random vector pool. Unit rotation vectors in the XZ plane, Y coord goes between [-1.0, 1.0].
	 * Filled at init time */
	public Double3[] pRot = new Double3[64];
	
	/** Pool of random double values. Filled at init time. */
	public double[] pRand = new double[64];
	
	/** Indicates AO should be disabled. */
	public boolean noShading = false;
	
	public ShadingValues aoXPYZPP = new ShadingValues();
	public ShadingValues aoXPYZPN = new ShadingValues();
	public ShadingValues aoXPYZNP = new ShadingValues();
	public ShadingValues aoXPYZNN = new ShadingValues();
	public ShadingValues aoXNYZPP = new ShadingValues();
	public ShadingValues aoXNYZPN = new ShadingValues();
	public ShadingValues aoXNYZNP = new ShadingValues();
	public ShadingValues aoXNYZNN = new ShadingValues();
	public ShadingValues aoYPXZPP = new ShadingValues();
	public ShadingValues aoYPXZPN = new ShadingValues();
	public ShadingValues aoYPXZNP = new ShadingValues();
	public ShadingValues aoYPXZNN = new ShadingValues();
	public ShadingValues aoYNXZPP = new ShadingValues();
	public ShadingValues aoYNXZPN = new ShadingValues();
	public ShadingValues aoYNXZNP = new ShadingValues();
	public ShadingValues aoYNXZNN = new ShadingValues();
	public ShadingValues aoZPXYPP = new ShadingValues();
	public ShadingValues aoZPXYPN = new ShadingValues();
	public ShadingValues aoZPXYNP = new ShadingValues();
	public ShadingValues aoZPXYNN = new ShadingValues();
	public ShadingValues aoZNXYPP = new ShadingValues();
	public ShadingValues aoZNXYPN = new ShadingValues();
	public ShadingValues aoZNXYNP = new ShadingValues();
	public ShadingValues aoZNXYNN = new ShadingValues();

	// temporary shading values for a single face
	public ShadingValues faceAOPP, faceAOPN, faceAONN, faceAONP;
	
    /** Quick lookup array to get AO values of a given block corner */
    protected ShadingValues[][][] shadingLookup = new ShadingValues[6][6][6];
    
	/** Initialize random values and lookup array */
	public void init() {
		List<Double3> perturbs = new ArrayList<Double3>(64);
		for (int idx = 0; idx < 64; idx++) {
			double angle = (double) idx * Math.PI * 2.0 / 64.0;
			perturbs.add(new Double3(Math.cos(angle), Math.random() * 2.0 - 1.0, Math.sin(angle)));
			pRand[idx] = Math.random();
		}
		Collections.shuffle(perturbs);
		Iterator<Double3> iter = perturbs.iterator();
		for (int idx = 0; idx < 64; idx++) pRot[idx] = iter.next();
		
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

    protected ShadingValues getAoLookupMax(ForgeDirection primary, ForgeDirection secondary, ForgeDirection tertiary) {
    	ShadingValues pri = shadingLookup[primary.ordinal()][secondary.ordinal()][tertiary.ordinal()];
    	ShadingValues sec = shadingLookup[secondary.ordinal()][primary.ordinal()][tertiary.ordinal()];
//    	ShadingValues ter = shadingLookup[tertiary.ordinal()][primary.ordinal()][secondary.ordinal()];
    	return pri.green > sec.green ? pri :sec;
//    	return pri.green > sec.green && pri.green > ter.green ? pri : (sec.green > ter.green ? sec : ter);
    }

    
	/** Get a semi-random value depending on block position.
	 * @param x block X coord
	 * @param y block Y coord
	 * @param z block Z coord
	 * @param seed additional seed
	 * @return semirandom value
	 */
	protected int getSemiRandomFromPos(double x, double y, double z, int seed) {
		long lx = MathHelper.floor_double(x);
		long ly = MathHelper.floor_double(y);
		long lz = MathHelper.floor_double(z);
		long value = (lx * lx + ly * ly + lz * lz + lx * ly + ly * lz + lz * lx + seed * seed) & 63;
		value = (3 * lx * value + 5 * ly * value + 7 * lz * value + 11 * seed) & 63;
		return (int) value;
	}
	
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		renderStandardBlockAsItem(renderer, block, metadata, 1.0f);
	}
	
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
	
	public int getRenderId() {
		return 0;
	}
	
	/** Render a block normally, and extract AO data
	 * @param pass extract data from rendering pass with this index
	 * @param world the World
	 * @param x x coord
	 * @param y y coord
	 * @param z z coord
	 * @param block the block
	 * @param modelId render ID of block
	 * @param renderer the renderer to use
	 * @return true if rendering block breaking overlay
	 */
	protected boolean renderWorldBlockBase(int pass, IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		// use original renderer for block breaking overlay
		if (renderer.hasOverrideBlockTexture()) {
			renderer.setRenderBoundsFromBlock(block);
			renderer.renderStandardBlock(block, x, y, z);
			return true;
		}
		
		// render block
		noShading = block.getLightValue() != 0;
		drawPass = 0;
		setAOPassCounters(pass);
		setRenderBoundsFromBlock(block);
		ISimpleBlockRenderingHandler handler = RenderUtils.getRenderingHandler(block.getRenderType());
		if (handler != null && block.getRenderType() != 0) {
			handler.renderWorldBlock(world, x, y, z, block, block.getRenderType(), this);
		} else {
			renderStandardBlock(block, x, y, z);			
		}
		return false;
	}
	
	protected void renderStandardBlockAsItem(RenderBlocks renderer, Block p_147800_1_, int p_147800_2_, float p_147800_3_) {
		Tessellator tessellator = Tessellator.instance;
		boolean flag = p_147800_1_ == Blocks.grass;
		
        float f2;
        float f3;
        int k;
        
		p_147800_1_.setBlockBoundsForItemRender();
		renderer.setRenderBoundsFromBlock(p_147800_1_);
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 0, p_147800_2_));
        tessellator.draw();

        if (flag && renderer.useInventoryTint)
        {
            k = p_147800_1_.getRenderColor(p_147800_2_);
            f2 = (float)(k >> 16 & 255) / 255.0F;
            f3 = (float)(k >> 8 & 255) / 255.0F;
            float f4 = (float)(k & 255) / 255.0F;
            GL11.glColor4f(f2 * p_147800_3_, f3 * p_147800_3_, f4 * p_147800_3_, 1.0F);
        }

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 1, p_147800_2_));
        tessellator.draw();

        if (flag && renderer.useInventoryTint)
        {
            GL11.glColor4f(p_147800_3_, p_147800_3_, p_147800_3_, 1.0F);
        }

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 2, p_147800_2_));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 3, p_147800_2_));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 4, p_147800_2_));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(p_147800_1_, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(p_147800_1_, 5, p_147800_2_));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}
	
	protected void setShadingForFace(ForgeDirection dir) {
		if (dir == ForgeDirection.DOWN) {
			// dir1 WEST, dir2 NORTH
			faceAOPP = aoYNXZNN; faceAOPN = aoYNXZNP; faceAONN = aoYNXZPP; faceAONP = aoYNXZPN;
		} else if (dir == ForgeDirection.UP) {
			// dir1 WEST, dir2 SOUTH
			faceAOPP = aoYPXZNP; faceAOPN = aoYPXZNN; faceAONN = aoYPXZPN; faceAONP = aoYPXZPP;
		} else if (dir == ForgeDirection.NORTH) {
			// dir1 WEST, dir2 UP
			faceAOPP = aoZNXYNP; faceAOPN = aoZNXYNN; faceAONN = aoZNXYPN; faceAONP = aoZNXYPP;
		} else if (dir == ForgeDirection.SOUTH) {
			// dir1 EAST, dir2 UP
			faceAOPP = aoZPXYPP; faceAOPN = aoZPXYPN; faceAONN = aoZPXYNN; faceAONP = aoZPXYNP;
		} else if (dir == ForgeDirection.WEST) {
			// dir1 SOUTH, dir2 UP
			faceAOPP = aoXNYZPP; faceAOPN = aoXNYZNP; faceAONN = aoXNYZNN; faceAONP = aoXNYZPN;
		} else if (dir == ForgeDirection.EAST) {
			// dir1 NORTH, dir2 UP
			faceAOPP = aoXPYZPN; faceAOPN = aoXPYZNN; faceAONN = aoXPYZNP; faceAONP = aoXPYZPP;
		}
	}
	
	public void renderCrossedSideQuads(Double3 drawBase, ForgeDirection dir, double scale, double halfHeight, Double3 rendomVec, double offset, IIcon renderIcon, int uvRot, boolean noShading) {
		Double3 facePP, faceNP, faceNormal, drawCenter;
		
		if (dir == ForgeDirection.UP) {
			// special case for block top, we'll be rendering a LOT of those
			facePP = new Double3(-scale, 0.0, scale);
			faceNP = new Double3(scale, 0.0, scale);
			faceNormal = new Double3(0.0, halfHeight, 0.0);
			drawCenter = drawBase.add(faceNormal);
			if (rendomVec != null) {
				drawCenter = drawBase.add(faceNormal).add(rendomVec.scaleAxes(-offset, 0.0, offset));
			}
		} else {
			facePP = new Double3(faceDir1[dir.ordinal()]).add(new Double3(faceDir2[dir.ordinal()])).scale(scale);
			faceNP = new Double3(faceDir1[dir.ordinal()]).inverse().add(new Double3(faceDir2[dir.ordinal()])).scale(scale);
			faceNormal = new Double3(dir).scale(halfHeight);
			drawCenter = drawBase.add(faceNormal);
			if (rendomVec != null) {
				drawCenter = drawCenter.add(new Double3(faceDir1[dir.ordinal()]).scale(rendomVec.x).scale(offset))
									   .add(new Double3(faceDir2[dir.ordinal()]).scale(rendomVec.z).scale(offset));
			}
		}
		
		if (Minecraft.isAmbientOcclusionEnabled() && !noShading && !this.noShading) {
			setShadingForFace(dir);
			renderQuadWithShading(renderIcon, drawCenter, facePP, faceNormal, uvRot,			faceAOPP, faceAONN, faceAONN, faceAOPP);
			renderQuadWithShading(renderIcon, drawCenter, facePP.inverse(), faceNormal, uvRot,	faceAONN, faceAOPP, faceAOPP, faceAONN);
			renderQuadWithShading(renderIcon, drawCenter, faceNP, faceNormal, uvRot, 			faceAONP, faceAOPN, faceAOPN, faceAONP);
			renderQuadWithShading(renderIcon, drawCenter, faceNP.inverse(), faceNormal, uvRot,	faceAOPN, faceAONP, faceAONP, faceAOPN);
		} else {
			renderQuad(renderIcon, drawCenter, facePP, faceNormal, uvRot);
			renderQuad(renderIcon, drawCenter, facePP.inverse(), faceNormal, uvRot);
			renderQuad(renderIcon, drawCenter, faceNP, faceNormal, uvRot);
			renderQuad(renderIcon, drawCenter, faceNP.inverse(), faceNormal, uvRot);
		}
	}
	
	protected void renderCrossedBlockQuadsTranslate(ForgeDirection axisMain, Double3 blockCenter, double halfSize, Double3 offsetVec, IIcon crossLeafIcon, int uvRot) {
		Double3 drawCenter = blockCenter;
		if (offsetVec != null) drawCenter = drawCenter.add(offsetVec);
		
		Double3 axis1 = new Double3(faceDir1[axisMain.ordinal()]);
		Double3 axis2 = new Double3(faceDir2[axisMain.ordinal()]);
		
		Double3 horz1 = axis1.add(axis2).scale(halfSize);
		Double3 horz2 = axis1.sub(axis2).scale(halfSize);
		Double3 vert1 = new Double3(axisMain).scale(halfSize * 1.41);
		
		renderCrossedBlockQuadsInternal(axisMain, drawCenter, horz1, horz2, vert1, crossLeafIcon, uvRot);
	}
	
	protected void renderCrossedBlockQuadsSkew(ForgeDirection axisMain, Double3 blockCenter, double halfSize, Double3 offsetVec1, Double3 offsetVec2, IIcon crossLeafIcon, int uvRot) {
		Double3 axis1 = new Double3(faceDir1[axisMain.ordinal()]);
		Double3 axis2 = new Double3(faceDir2[axisMain.ordinal()]);
		
		Double3 horz1 = axis1.add(axis2).scale(halfSize).add(axis1.scale(offsetVec1.x)).add(axis2.scale(offsetVec1.z)).add(new Double3(axisMain).scale(offsetVec1.y));
		Double3 horz2 = axis1.sub(axis2).scale(halfSize).add(axis1.scale(offsetVec2.x)).add(axis2.scale(offsetVec2.z)).add(new Double3(axisMain).scale(offsetVec2.y));
		Double3 vert1 = new Double3(axisMain).scale(halfSize * 1.41);
		
		renderCrossedBlockQuadsInternal(axisMain, blockCenter, horz1, horz2, vert1, crossLeafIcon, uvRot);
	}
	
	private void renderCrossedBlockQuadsInternal(ForgeDirection axisMain, Double3 drawCenter, Double3 horz1, Double3 horz2, Double3 vert1, IIcon crossLeafIcon, int uvRot) {
		if (Minecraft.isAmbientOcclusionEnabled() && !noShading) {
			ForgeDirection axis1P = faceDir1[axisMain.ordinal()];
			ForgeDirection axis2P = faceDir2[axisMain.ordinal()];
			ForgeDirection axis1N = axis1P.getOpposite();
			ForgeDirection axis2N = axis2P.getOpposite();
			
			renderQuadWithShading(crossLeafIcon, drawCenter, horz1, vert1, uvRot,
					getAoLookupMax(axisMain, axis2P, axis1P),
					getAoLookupMax(axisMain, axis1N, axis2N),
					getAoLookupMax(axisMain.getOpposite(), axis1N, axis2N),
					getAoLookupMax(axisMain.getOpposite(), axis2P, axis1P));
			renderQuadWithShading(crossLeafIcon, drawCenter, horz1.inverse(), vert1, uvRot,
					getAoLookupMax(axisMain, axis2N, axis1N),
					getAoLookupMax(axisMain, axis1P, axis2P),
					getAoLookupMax(axisMain.getOpposite(), axis1P, axis2P),
					getAoLookupMax(axisMain.getOpposite(), axis2N, axis1N));
			renderQuadWithShading(crossLeafIcon, drawCenter, horz2, vert1, uvRot,
					getAoLookupMax(axisMain, axis1P, axis2N),
					getAoLookupMax(axisMain, axis2P, axis1N),
					getAoLookupMax(axisMain.getOpposite(), axis2P, axis1N),
					getAoLookupMax(axisMain.getOpposite(), axis1P, axis2N));
			renderQuadWithShading(crossLeafIcon, drawCenter, horz2.inverse(), vert1, uvRot,
					getAoLookupMax(axisMain, axis1N, axis2P),
					getAoLookupMax(axisMain, axis2N, axis1P),
					getAoLookupMax(axisMain.getOpposite(), axis2N, axis1P),
					getAoLookupMax(axisMain.getOpposite(), axis1N, axis2P));
		} else {
			renderQuad(crossLeafIcon, drawCenter, horz1, vert1, uvRot);
			renderQuad(crossLeafIcon, drawCenter, horz1.inverse(), vert1, uvRot);
			renderQuad(crossLeafIcon, drawCenter, horz2, vert1, uvRot);
			renderQuad(crossLeafIcon, drawCenter, horz2.inverse(), vert1, uvRot);
		}
	}
	
	@Override
	public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceZNeg(block, x, y, z, icon);
		saveShadingTopLeft(aoZNXYPP);
		saveShadingTopRight(aoZNXYNP);
		saveShadingBottomLeft(aoZNXYPN);
		saveShadingBottomRight(aoZNXYNN);
	}

	@Override
	public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceZPos(block, x, y, z, icon);
		saveShadingTopLeft(aoZPXYNP);
		saveShadingTopRight(aoZPXYPP);
		saveShadingBottomLeft(aoZPXYNN);
		saveShadingBottomRight(aoZPXYPN);
	}

	@Override
	public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceXNeg(block, x, y, z, icon);
		saveShadingTopLeft(aoXNYZPN);
		saveShadingTopRight(aoXNYZPP);
		saveShadingBottomLeft(aoXNYZNN);
		saveShadingBottomRight(aoXNYZNP);
	}

	@Override
	public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceXPos(block, x, y, z, icon);
		saveShadingTopLeft(aoXPYZPP);
		saveShadingTopRight(aoXPYZPN);
		saveShadingBottomLeft(aoXPYZNP);
		saveShadingBottomRight(aoXPYZNN);
	}

	@Override
	public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceYNeg(block, x, y, z, icon);
		saveShadingTopLeft(aoYNXZNP);
		saveShadingTopRight(aoYNXZPP);
		saveShadingBottomLeft(aoYNXZNN);
		saveShadingBottomRight(aoYNXZPN);
	}

	@Override
	public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
		if (!skipFaces) super.renderFaceYPos(block, x, y, z, icon);
		saveShadingTopLeft(aoYPXZPP);
		saveShadingTopRight(aoYPXZNP);
		saveShadingBottomLeft(aoYPXZPN);
		saveShadingBottomRight(aoYPXZNN);
	}
	
	protected void saveShadingTopLeft(ShadingValues values) {
		if (--values.passCounter < 0) return;
		values.brightness = brightnessTopLeft;
		values.red = colorRedTopLeft;
		values.green = colorGreenTopLeft;
		values.blue = colorBlueTopLeft;
	}
	
	protected void saveShadingTopRight(ShadingValues values) {
		if (--values.passCounter < 0) return;
		values.brightness = brightnessTopRight;
		values.red = colorRedTopRight;
		values.green = colorGreenTopRight;
		values.blue = colorBlueTopRight;
	}
	
	protected void saveShadingBottomLeft(ShadingValues values) {
		if (--values.passCounter < 0) return;
		values.brightness = brightnessBottomLeft;
		values.red = colorRedBottomLeft;
		values.green = colorGreenBottomLeft;
		values.blue = colorBlueBottomLeft;
	}
	
	protected void saveShadingBottomRight(ShadingValues values) {
		if (--values.passCounter < 0) return;
		values.brightness = brightnessBottomRight;
		values.red = colorRedBottomRight;
		values.green = colorGreenBottomRight;
		values.blue = colorBlueBottomRight;
	}
	
	/** Set pass counter on all shading value objects.
	 *  Used to collect AO values from a specific draw pass
	 *  if the underlying renderer draws overlays 
	 * @param value pass counter
	 */
	protected void setAOPassCounters(int value) {
		aoXPYZPP.passCounter = value;
		aoXPYZPN.passCounter = value;
		aoXPYZNP.passCounter = value;
		aoXPYZNN.passCounter = value;
		aoXNYZPP.passCounter = value;
		aoXNYZPN.passCounter = value;
		aoXNYZNP.passCounter = value;
		aoXNYZNN.passCounter = value;
		aoYPXZPP.passCounter = value;
		aoYPXZPN.passCounter = value;
		aoYPXZNP.passCounter = value;
		aoYPXZNN.passCounter = value;
		aoYNXZPP.passCounter = value;
		aoYNXZPN.passCounter = value;
		aoYNXZNP.passCounter = value;
		aoYNXZNN.passCounter = value;
		aoZPXYPP.passCounter = value;
		aoZPXYPN.passCounter = value;
		aoZPXYNP.passCounter = value;
		aoZPXYNN.passCounter = value;
		aoZNXYPP.passCounter = value;
		aoZNXYPN.passCounter = value;
		aoZNXYNP.passCounter = value;
		aoZNXYNN.passCounter = value;
	}
	
	/** Render textured quad
	 * @param icon texture to use
	 * @param center center of quad
	 * @param vec1 vector to the half-point of one of the sides
	 * @param vec2 vector to half-point of side next to vec1
	 * @param uvRot number of increments to rotate UV coordinates by
	 */
	protected void renderQuad(IIcon icon, Double3 center, Double3 vec1, Double3 vec2, int uvRot) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.addVertexWithUV(center.x + vec1.x + vec2.x, center.y + vec1.y + vec2.y, center.z + vec1.z + vec2.z, icon.getInterpolatedU(uValues[uvRot & 3]), icon.getInterpolatedV(vValues[uvRot & 3]));
		tessellator.addVertexWithUV(center.x - vec1.x + vec2.x, center.y - vec1.y + vec2.y, center.z - vec1.z + vec2.z, icon.getInterpolatedU(uValues[(uvRot + 1) & 3]), icon.getInterpolatedV(vValues[(uvRot + 1) & 3]));
		tessellator.addVertexWithUV(center.x - vec1.x - vec2.x, center.y - vec1.y - vec2.y, center.z - vec1.z - vec2.z, icon.getInterpolatedU(uValues[(uvRot + 2) & 3]), icon.getInterpolatedV(vValues[(uvRot + 2) & 3]));
		tessellator.addVertexWithUV(center.x + vec1.x - vec2.x, center.y + vec1.y - vec2.y, center.z + vec1.z - vec2.z, icon.getInterpolatedU(uValues[(uvRot + 3) & 3]), icon.getInterpolatedV(vValues[(uvRot + 3) & 3]));
	}
	
	/** Render textured quad using AO information
	 * @param icon texture to use
	 * @param center center of quad
	 * @param vec1 vector to the half-point of one of the sides
	 * @param vec2 vector to half-point of side next to vec1
	 * @param uvRot number of increments to rotate UV coordinates by
	 * @param aoPP AO values for vertex at (+vec1, +vec2)
	 * @param aoNP AO values for vertex at (-vec1, +vec2)
	 * @param aoNN AO values for vertex at (-vec1, -vec2)
	 * @param aoPN AO values for vertex at (+vec1, -vec2) 
	 */
	protected void renderQuadWithShading(IIcon icon, Double3 center, Double3 vec1, Double3 vec2, int uvRot, ShadingValues aoPP, ShadingValues aoNP, ShadingValues aoNN, ShadingValues aoPN) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.setBrightness(aoPP.brightness);
		tessellator.setColorOpaque_F(aoPP.red, aoPP.green, aoPP.blue);
		tessellator.addVertexWithUV(center.x + vec1.x + vec2.x, center.y + vec1.y + vec2.y, center.z + vec1.z + vec2.z, icon.getInterpolatedU(uValues[uvRot & 3]), icon.getInterpolatedV(vValues[uvRot & 3]));
		tessellator.setBrightness(aoNP.brightness);
		tessellator.setColorOpaque_F(aoNP.red, aoNP.green, aoNP.blue);
		tessellator.addVertexWithUV(center.x - vec1.x + vec2.x, center.y - vec1.y + vec2.y, center.z - vec1.z + vec2.z, icon.getInterpolatedU(uValues[(uvRot + 1) & 3]), icon.getInterpolatedV(vValues[(uvRot + 1) & 3]));
		tessellator.setBrightness(aoNN.brightness);
		tessellator.setColorOpaque_F(aoNN.red, aoNN.green, aoNN.blue);
		tessellator.addVertexWithUV(center.x - vec1.x - vec2.x, center.y - vec1.y - vec2.y, center.z - vec1.z - vec2.z, icon.getInterpolatedU(uValues[(uvRot + 2) & 3]), icon.getInterpolatedV(vValues[(uvRot + 2) & 3]));
		tessellator.setBrightness(aoPN.brightness);
		tessellator.setColorOpaque_F(aoPN.red, aoPN.green, aoPN.blue);
		tessellator.addVertexWithUV(center.x + vec1.x - vec2.x, center.y + vec1.y - vec2.y, center.z + vec1.z - vec2.z, icon.getInterpolatedU(uValues[(uvRot + 3) & 3]), icon.getInterpolatedV(vValues[(uvRot + 3) & 3]));
	}
	
	protected void renderQuad(IIcon icon, Double3 vert1, Double3 vert2, Double3 vert3, Double3 vert4, double[] uValues, double[] vValues, ShadingValues shading1, ShadingValues shading2, ShadingValues shading3, ShadingValues shading4) {
	    Tessellator tessellator = Tessellator.instance;
	    
	    if (shading1 != null) {
    	    tessellator.setBrightness(shading1.brightness);
    	    tessellator.setColorOpaque_F(shading1.red, shading1.green, shading1.blue);
	    }
	    tessellator.addVertexWithUV(vert1.x, vert1.y, vert1.z, icon.getInterpolatedU(uValues[0]), icon.getInterpolatedV(vValues[0]));
	    
	    if (shading2 != null) {
            tessellator.setBrightness(shading2.brightness);
            tessellator.setColorOpaque_F(shading2.red, shading2.green, shading2.blue);
	    }
        tessellator.addVertexWithUV(vert2.x, vert2.y, vert2.z, icon.getInterpolatedU(uValues[1]), icon.getInterpolatedV(vValues[1]));
        
        if (shading3 != null) {
            tessellator.setBrightness(shading3.brightness);
            tessellator.setColorOpaque_F(shading3.red, shading3.green, shading3.blue);
        }
        tessellator.addVertexWithUV(vert3.x, vert3.y, vert3.z, icon.getInterpolatedU(uValues[2]), icon.getInterpolatedV(vValues[2]));
        
        if (shading4 != null) {
            tessellator.setBrightness(shading4.brightness);
            tessellator.setColorOpaque_F(shading4.red, shading4.green, shading4.blue);
        }
        tessellator.addVertexWithUV(vert4.x, vert4.y, vert4.z, icon.getInterpolatedU(uValues[3]), icon.getInterpolatedV(vValues[3]));
	}
	
	protected int getBrightness(Block block, int x, int y, int z) {
		return block.getMixedBrightnessForBlock(blockAccess, x, y, z);
	}
	
	/** Get the average shading values of the 4 AO data points of a face
	 * @param dir face direction
	 * @return average
	 */
	protected ShadingValues avgShadingForFace(ForgeDirection dir) {
	    setShadingForFace(dir);
	    return CLCIntegration.avgShading(CLCIntegration.avgShading(faceAONN, faceAONP), CLCIntegration.avgShading(faceAOPN, faceAOPP));
	}
	
	protected void setAOColors(int color) {
	    float red = ((color >> 16) & 0xFF) / 255.0f;
	    float green = ((color >> 8) & 0xFF) / 255.0f;
	    float blue = (color & 0xFF) / 255.0f;

        colorRedTopLeft *= red;
        colorRedTopRight *= red;
        colorRedBottomLeft *= red;
        colorRedBottomRight *= red;
	    
	    colorGreenTopLeft *= green;
	    colorGreenTopRight *= green;
	    colorGreenBottomLeft *= green;
	    colorGreenBottomRight *= green;
	        
	    colorBlueTopLeft *= blue;
	    colorBlueTopRight *= blue;
	    colorBlueBottomLeft *= blue;
	    colorBlueBottomRight *= blue;
    }

    @Override
    public boolean renderStandardBlock(Block p_147784_1_, int p_147784_2_, int p_147784_3_, int p_147784_4_) {
        drawPass++;
        try {
            return super.renderStandardBlock(p_147784_1_, p_147784_2_, p_147784_3_, p_147784_4_);
        } catch (ArrayIndexOutOfBoundsException e) {
            BetterFoliage.log.warn(String.format("ArrayIndexOutOfBounds when rendering block: %s, meta: %d"), p_147784_1_.getClass().getName(), blockAccess.getBlockMetadata(p_147784_2_, p_147784_3_, p_147784_4_));
            return true;
        }
    }
	
	protected int getCameraDistance(int x, int y, int z) {
		EntityLivingBase camera = Minecraft.getMinecraft().renderViewEntity;
		if (camera == null) return 0;
		int result = Math.abs(x - MathHelper.floor_double(camera.posX));
		result += Math.abs(y - MathHelper.floor_double(camera.posY));
		result += Math.abs(z - MathHelper.floor_double(camera.posZ));
		return result;
	}
}
