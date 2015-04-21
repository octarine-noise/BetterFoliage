package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.IShadingData;
import mods.betterfoliage.client.render.Rotation;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class OctaPrismQuadrantQuads implements IQuadCollection {

	public static boolean useMax = false;
	
	public Double3 originCorner;
	
	public EnumFacing horz1Dir;
	
	public EnumFacing horz2Dir;
	
	public EnumFacing vertDir;
	
	public double chamferSize;
	
	public int uvRot;
	
	public DynamicQuad dir1Mid;
	
	public DynamicQuad dir1Cham;
	
	public DynamicQuad dir2Cham;
	
	public DynamicQuad dir2Mid;
	
	public DynamicQuad topLid1;
	
	public DynamicQuad topLid2;
	
	public DynamicQuad bottomLid1;
	
	public DynamicQuad bottomLid2;

	
	private OctaPrismQuadrantQuads() {}
	
	public static OctaPrismQuadrantQuads create(Double3 originCorner, EnumFacing horz1Dir, EnumFacing horz2Dir, EnumFacing vertDir, double chamferSize, int uvRot, boolean hasTop, boolean hasBottom) {
		OctaPrismQuadrantQuads result = new OctaPrismQuadrantQuads();
		
		result.chamferSize = chamferSize;
		result.uvRot = uvRot;
		result.horz1Dir = horz1Dir;
		result.horz2Dir = horz2Dir;
		result.vertDir = vertDir;
		
	    Double3 horz1 = new Double3(horz1Dir);
	    Double3 horz2 = new Double3(horz2Dir);
	    Double3 vert = new Double3(vertDir);
        Double3 mid1 = originCorner.add(horz1.scale(0.5));
        Double3 cham1 = originCorner.add(horz1.scale(chamferSize));
        Double3 mid2 = originCorner.add(horz2.scale(0.5));
        Double3 cham2 = originCorner.add(horz2.scale(chamferSize));
        Double3 chamCenter = cham1.add(cham2).scale(0.5);
        Double3 blockCenter = originCorner.add(horz1.scale(0.5)).add(horz2.scale(0.5));
		
        result.dir1Mid = DynamicQuad.createParallelogramExtruded(mid1, cham1, vert);
        result.dir1Cham = DynamicQuad.createParallelogramExtruded(cham1, chamCenter, vert);
        result.dir2Cham = DynamicQuad.createParallelogramExtruded(chamCenter, cham2, vert);
        result.dir2Mid = DynamicQuad.createParallelogramExtruded(cham2, mid2, vert);
		
        if (hasTop) {
        	result.topLid1 = DynamicQuad.createFromVertices(blockCenter.add(vert), mid1.add(vert), cham1.add(vert), chamCenter.add(vert));
        	result.topLid2 = DynamicQuad.createFromVertices(cham2.add(vert), mid2.add(vert), blockCenter.add(vert), chamCenter.add(vert));
        }
        if (hasBottom) {
        	result.bottomLid1 = DynamicQuad.createFromVertices(blockCenter, chamCenter, cham1, mid1);
        	result.bottomLid2 = DynamicQuad.createFromVertices(cham2, chamCenter, blockCenter, mid2);
        }
		
		return result;
	}
	
	@Override
	public IQuadCollection setBrightness(IShadingData shadingData) {
		EnumFacing horz1DirOpp = horz1Dir.getOpposite();
		EnumFacing horz2DirOpp = horz2Dir.getOpposite();
		EnumFacing vertDirOpp = vertDir.getOpposite();
		
		int br1FarBottom = shadingData.getBrightness(horz2DirOpp, horz1Dir, vertDirOpp, useMax);
		int br1NearBottom = shadingData.getBrightness(horz2DirOpp, horz1DirOpp, vertDirOpp, useMax);
		int br1FarTop = shadingData.getBrightness(horz2DirOpp, horz1Dir, vertDir, useMax);
		int br1NearTop = shadingData.getBrightness(horz2DirOpp, horz1DirOpp, vertDir, useMax);
		int br1MidTop = averageBrightness(br1FarTop, br1NearTop);
		int br1MidBottom = averageBrightness(br1FarBottom, br1NearBottom);
		
		int br2FarBottom = shadingData.getBrightness(horz1DirOpp, horz2Dir, vertDirOpp, useMax);
		int br2NearBottom = shadingData.getBrightness(horz1DirOpp, horz2DirOpp, vertDirOpp, useMax);
		int br2FarTop = shadingData.getBrightness(horz1DirOpp, horz2Dir, vertDir, useMax);
		int br2NearTop = shadingData.getBrightness(horz1DirOpp, horz2DirOpp, vertDir, useMax);
		int br2MidTop = averageBrightness(br2FarTop, br2NearTop);
		int br2MidBottom = averageBrightness(br2FarBottom, br2NearBottom);
		
		int brAvgSideTop = averageBrightness(br1NearTop, br2NearTop);
		int brAvgSideBottom = averageBrightness(br1NearBottom, br2NearBottom);
		
		int brTopOrigin = shadingData.getBrightness(vertDir, horz1DirOpp, horz2DirOpp, useMax);
		int brTopDir1 = shadingData.getBrightness(vertDir, horz1Dir, horz2DirOpp, useMax);
		int brTopDir2 = shadingData.getBrightness(vertDir, horz1DirOpp, horz2Dir, useMax);
		int brTopOpposite = shadingData.getBrightness(vertDir, horz1Dir, horz2Dir, useMax);
		int brTopMid1 = averageBrightness(brTopOrigin, brTopDir1);
		int brTopMid2 = averageBrightness(brTopOrigin, brTopDir2);
		
		int brBottomOrigin = shadingData.getBrightness(vertDirOpp, horz1DirOpp, horz2DirOpp, useMax);
		int brBottomDir1 = shadingData.getBrightness(vertDirOpp, horz1Dir, horz2DirOpp, useMax);
		int brBottomDir2 = shadingData.getBrightness(vertDirOpp, horz1DirOpp, horz2Dir, useMax);
		int brBottomOpposite = shadingData.getBrightness(vertDirOpp, horz1Dir, horz2Dir, useMax);
		int brBottomMid1 = averageBrightness(brBottomOrigin, brBottomDir1);
		int brBottomMid2 = averageBrightness(brBottomOrigin, brBottomDir2);
		
		int brTopCenter = averageBrightness(brTopOrigin, brTopDir1, brTopDir2, brTopOpposite);
		int brBottomCenter = averageBrightness(brBottomOrigin, brBottomDir1, brBottomDir2, brBottomOpposite);
		
		dir1Mid.setBrightness(br1MidBottom, br1NearBottom, br1NearTop, br1MidTop);
		dir1Cham.setBrightness(br1NearBottom, brAvgSideBottom, brAvgSideTop, br1NearTop);
		dir2Cham.setBrightness(brAvgSideBottom, br2NearBottom, br2NearTop, brAvgSideTop);
		dir2Mid.setBrightness(br2NearBottom, br2MidBottom, br2MidTop, br2NearTop);
		
		if (topLid1 != null) {
			topLid1.setBrightness(brTopCenter, brTopMid1, brTopOrigin, brTopOrigin);
			topLid2.setBrightness(brTopOrigin, brTopMid2, brTopCenter, brTopOrigin);
		}
		if (bottomLid1 != null) {
			bottomLid1.setBrightness(brBottomCenter, brBottomOrigin, brBottomOrigin, brBottomMid1);
			bottomLid2.setBrightness(brBottomOrigin, brBottomOrigin, brBottomCenter, brBottomMid2);
		}
		
		return this;
	}

	@Override
	public IQuadCollection setColor(IShadingData shadingData, Color4 color) {
		if (shadingData.shouldUseAO()) {
			EnumFacing horz1DirOpp = horz1Dir.getOpposite();
			EnumFacing horz2DirOpp = horz2Dir.getOpposite();
			EnumFacing vertDirOpp = vertDir.getOpposite();
			
			Color4 col1FarBottom = color.multiply(shadingData.getColorMultiplier(horz2DirOpp, horz1Dir, vertDirOpp, useMax));
			Color4 col1NearBottom = color.multiply(shadingData.getColorMultiplier(horz2DirOpp, horz1DirOpp, vertDirOpp, useMax));
			Color4 col1FarTop = color.multiply(shadingData.getColorMultiplier(horz2DirOpp, horz1Dir, vertDir, useMax));
			Color4 col1NearTop = color.multiply(shadingData.getColorMultiplier(horz2DirOpp, horz1DirOpp, vertDir, useMax));
			Color4 col1MidTop = Color4.average(col1FarTop, col1NearTop);
			Color4 col1MidBottom = Color4.average(col1FarBottom, col1NearBottom);
			
			Color4 col2FarBottom = color.multiply(shadingData.getColorMultiplier(horz1DirOpp, horz2Dir, vertDirOpp, useMax));
			Color4 col2NearBottom = color.multiply(shadingData.getColorMultiplier(horz1DirOpp, horz2DirOpp, vertDirOpp, useMax));
			Color4 col2FarTop = color.multiply(shadingData.getColorMultiplier(horz1DirOpp, horz2Dir, vertDir, useMax));
			Color4 col2NearTop = color.multiply(shadingData.getColorMultiplier(horz1DirOpp, horz2DirOpp, vertDir, useMax));
			Color4 col2MidTop = Color4.average(col2FarTop, col2NearTop);
			Color4 col2MidBottom = Color4.average(col2FarBottom, col2NearBottom);
			
			Color4 colAvgSideTop = Color4.average(col1NearTop, col2NearTop);
			Color4 colAvgSideBottom = Color4.average(col1NearBottom, col2NearBottom);
			
			Color4 colTopOrigin = color.multiply(shadingData.getColorMultiplier(vertDir, horz1DirOpp, horz2DirOpp, useMax));
			Color4 colTopDir1 = color.multiply(shadingData.getColorMultiplier(vertDir, horz1Dir, horz2DirOpp, useMax));
			Color4 colTopDir2 = color.multiply(shadingData.getColorMultiplier(vertDir, horz1DirOpp, horz2Dir, useMax));
			Color4 colTopOpposite = color.multiply(shadingData.getColorMultiplier(vertDir, horz1Dir, horz2Dir, useMax));
			Color4 colTopMid1 = Color4.average(colTopOrigin, colTopDir1);
			Color4 colTopMid2 = Color4.average(colTopOrigin, colTopDir2);
			
			Color4 colBottomOrigin = color.multiply(shadingData.getColorMultiplier(vertDirOpp, horz1DirOpp, horz2DirOpp, useMax));
			Color4 colBottomDir1 = color.multiply(shadingData.getColorMultiplier(vertDirOpp, horz1Dir, horz2DirOpp, useMax));
			Color4 colBottomDir2 = color.multiply(shadingData.getColorMultiplier(vertDirOpp, horz1DirOpp, horz2Dir, useMax));
			Color4 colBottomOpposite = color.multiply(shadingData.getColorMultiplier(vertDirOpp, horz1Dir, horz2Dir, useMax));
			Color4 colBottomMid1 = Color4.average(colBottomOrigin, colBottomDir1);
			Color4 colBottomMid2 = Color4.average(colBottomOrigin, colBottomDir2);
			
			Color4 colTopCenter = Color4.average(colTopOrigin, colTopDir1, colTopDir2, colTopOpposite);
			Color4 colBottomCenter = Color4.average(colBottomOrigin, colBottomDir1, colBottomDir2, colBottomOpposite);
			
			dir1Mid.setColor(col1MidBottom, col1NearBottom, col1NearTop, col1MidTop);
			dir1Cham.setColor(col1NearBottom, colAvgSideBottom, colAvgSideTop, col1NearTop);
			dir2Cham.setColor(colAvgSideBottom, col2NearBottom, col2NearTop, colAvgSideTop);
			dir2Mid.setColor(col2NearBottom, col2MidBottom, col2MidTop, col2NearTop);
			
			if (topLid1 != null) {
				topLid1.setColor(colTopCenter, colTopMid1, colTopOrigin, colTopOrigin);
				topLid2.setColor(colTopOrigin, colTopMid2, colTopCenter, colTopOrigin);
			}
			if (bottomLid1 != null && bottomLid2 != null) {
				bottomLid1.setColor(colBottomCenter, colBottomOrigin, colBottomOrigin, colBottomMid1);
				bottomLid2.setColor(colBottomOrigin, colBottomOrigin, colBottomCenter, colBottomMid2);
			}
		} else {
			dir1Mid.setColor(color);
			dir1Cham.setColor(color);
			dir2Cham.setColor(color);
			dir2Mid.setColor(color);
			
			if (topLid1 != null && topLid2 != null) {
				topLid1.setColor(color);
				topLid2.setColor(color);
			}
			if (bottomLid1 != null) {
				bottomLid1.setColor(color);
				bottomLid2.setColor(color);
			}
		}
		return this;
	}

	/** Not applicable. Use {@link OctaPrismQuadrantQuads#setTexture(TextureAtlasSprite, TextureAtlasSprite, TextureAtlasSprite, TextureAtlasSprite, double, int)} instead.}
	 * @see mods.betterfoliage.client.render.impl.primitives.IQuadCollection#setTexture(net.minecraft.client.renderer.texture.TextureAtlasSprite, int)
	 */
	@Override
	public IQuadCollection setTexture(TextureAtlasSprite texture, int uvRot) {
		return this;
	}
	
	public IQuadCollection setTexture(TextureAtlasSprite sideTexture, TextureAtlasSprite endTexture) {
		double[] vSides = new double[] {16.0, 16.0, 0.0, 0.0};
		
		double[] uValues = new double[] {0.0, 16.0, 16.0, 0.0};
		double[] vValues = new double[] {16.0, 16.0, 0.0, 0.0};
		
	    double[] uTop = new double[] {uValues[uvRot & 3], uValues[(uvRot + 1) & 3], uValues[(uvRot + 2) & 3], uValues[(uvRot + 3) & 3]};
	    double[] vTop = new double[] {vValues[uvRot & 3], vValues[(uvRot + 1) & 3], vValues[(uvRot + 2) & 3], vValues[(uvRot + 3) & 3]};
		
		double uLeft = chamferSize * 16.0;
        double uRight = 16.0 - uLeft;
        
		dir1Mid.setTexture(sideTexture, new double[]{8.0, uRight, uRight, 8.0}, vSides);
		dir1Cham.setTexture(sideTexture, new double[]{uRight, 16.0, 16.0, uRight}, vSides);
		dir2Cham.setTexture(sideTexture, new double[]{0.0, uLeft, uLeft, 0.0}, vSides);
		dir2Mid.setTexture(sideTexture, new double[]{uLeft, 8.0, 8.0, uLeft}, vSides);
		
		if (topLid1 != null) {
			topLid1.setTexture(endTexture,
					new double[]{8.0, (uTop[0] + uTop[3]) * 0.5, uTop[0] * (1 - chamferSize) + uTop[3] * chamferSize, uTop[0]},
					new double[]{8.0, (vTop[0] + vTop[3]) * 0.5, vTop[0] * (1 - chamferSize) + vTop[3] * chamferSize, vTop[0]});
			topLid2.setTexture(endTexture,
					new double[]{uTop[0] * (1 - chamferSize) + uTop[1] * chamferSize, (uTop[0] + uTop[1]) * 0.5, 8.0, uTop[0]},
					new double[]{vTop[0] * (1 - chamferSize) + vTop[1] * chamferSize, (vTop[0] + vTop[1]) * 0.5, 8.0, vTop[0]});
		}
		if (bottomLid1 != null) {
			bottomLid1.setTexture(endTexture,
					new double[]{8.0, uTop[0], uTop[0] * (1 - chamferSize) + uTop[3] * chamferSize, (uTop[0] + uTop[3]) * 0.5},
					new double[]{8.0, vTop[0], vTop[0] * (1 - chamferSize) + vTop[3] * chamferSize, (vTop[0] + vTop[3]) * 0.5});
			bottomLid2.setTexture(endTexture,
					new double[]{uTop[0] * (1 - chamferSize) + uTop[1] * chamferSize, uTop[0], 8.0, (uTop[0] + uTop[1]) * 0.5},
					new double[]{vTop[0] * (1 - chamferSize) + vTop[1] * chamferSize, vTop[0], 8.0, (vTop[0] + vTop[1]) * 0.5});
		}
		return this;
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		dir1Mid.render(renderer);
		dir1Cham.render(renderer);
		dir2Cham.render(renderer);
		dir2Mid.render(renderer);
		if (topLid1 != null) {
			topLid1.render(renderer);
			topLid2.render(renderer);
		}
		if (bottomLid1 != null) {
			bottomLid1.render(renderer);
			bottomLid2.render(renderer);
		}
	}

	@Override
	public void render(WorldRenderer renderer, Double3 translate) {
		dir1Mid.render(renderer, translate);
		dir1Cham.render(renderer, translate);
		dir2Cham.render(renderer, translate);
		dir2Mid.render(renderer, translate);
		if (topLid1 != null) {
			topLid1.render(renderer, translate);
			topLid2.render(renderer, translate);
		}
		if (bottomLid1 != null) {
			bottomLid1.render(renderer, translate);
			bottomLid2.render(renderer, translate);
		}
	}
	
	protected int averageBrightness(int br1, int br2) {
		return (br1 + br2) / 2;
	}
	
	protected int averageBrightness(int br1, int br2, int br3, int br4) {
		return (br1 + br2 + br3 + br4) / 4;
	}
	
	protected float averageColorMultiplier(float cm1, float cm2) {
		return (cm1 + cm2) * 0.5f;
	}
	
	protected float averageColorMultiplier(float cm1, float cm2, float cm3, float cm4) {
		return (cm1 + cm2 + cm3 + cm4) * 0.25f;
	}

	@Override
	public IQuadCollection transform(Rotation rotation) {
		dir1Mid.transform(rotation);
		dir1Cham.transform(rotation);
		dir2Cham.transform(rotation);
		dir2Mid.transform(rotation);
		if (topLid1 != null) {
			topLid1.transform(rotation);
			topLid2.transform(rotation);
		}
		if (bottomLid1 != null) {
			bottomLid1.transform(rotation);
			bottomLid2.transform(rotation);
		}
		return this;
	}
}
