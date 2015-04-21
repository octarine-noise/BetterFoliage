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
public class CubeQuadrantQuads implements IQuadCollection {

	public static boolean useMax = false;
	
	public Double3 originCorner;
	
	public EnumFacing horz1Dir;
	
	public EnumFacing horz2Dir;
	
	public EnumFacing vertDir;
	
	public int uvRot;
	
	public DynamicQuad dir1Face;
	
	public DynamicQuad dir2Face;
	
	public DynamicQuad topLid;
	
	public DynamicQuad bottomLid;
	
	private CubeQuadrantQuads() {}
	
	public static CubeQuadrantQuads create(Double3 originCorner, EnumFacing horz1Dir, EnumFacing horz2Dir, EnumFacing vertDir, int uvRot, boolean hasTop, boolean hasBottom) {
		CubeQuadrantQuads result = new CubeQuadrantQuads();
		
		result.uvRot = uvRot;
		result.horz1Dir = horz1Dir;
		result.horz2Dir = horz2Dir;
		result.vertDir = vertDir;
		
	    Double3 horz1 = new Double3(horz1Dir);
	    Double3 horz2 = new Double3(horz2Dir);
	    Double3 vert = new Double3(vertDir);
        Double3 mid1 = originCorner.add(horz1.scale(0.5));
        Double3 mid2 = originCorner.add(horz2.scale(0.5));
        Double3 blockCenter = originCorner.add(horz1.scale(0.5)).add(horz2.scale(0.5));
		
        result.dir1Face = DynamicQuad.createParallelogramExtruded(mid1, originCorner, vert);
        result.dir2Face = DynamicQuad.createParallelogramExtruded(originCorner, mid2, vert);
		
        if (hasTop) result.topLid = DynamicQuad.createFromVertices(originCorner.add(vert), mid2.add(vert), blockCenter.add(vert), mid1.add(vert));
        if (hasBottom) result.bottomLid = DynamicQuad.createFromVertices(mid1, blockCenter, mid2, originCorner);
		
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
		
		dir1Face.setBrightness(br1MidBottom, br1NearBottom, br1NearTop, br1MidTop);
		dir2Face.setBrightness(br2NearBottom, br2MidBottom, br2MidTop, br2NearTop);
		
		if (topLid != null) topLid.setBrightness(brTopOrigin, brTopMid2, brTopCenter, brTopMid1);
		if (bottomLid != null) bottomLid.setBrightness(brBottomMid1, brBottomCenter, brBottomMid2, brBottomOrigin);
		
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
			
			dir1Face.setColor(col1MidBottom, col1NearBottom, col1NearTop, col1MidTop);
			dir2Face.setColor(col2NearBottom, col2MidBottom, col2MidTop, col2NearTop);
			
			if (topLid != null) topLid.setColor(colTopOrigin, colTopMid2, colTopCenter, colTopMid1);
			if (bottomLid != null) bottomLid.setColor(colBottomMid1, colBottomCenter, colBottomMid2, colBottomOrigin);
		} else {
			dir1Face.setColor(color);
			dir2Face.setColor(color);
			
			if (topLid != null) topLid.setColor(color);
			if (bottomLid != null) bottomLid.setColor(color);
		}
		return this;
	}

	/** Not applicable. Use {@link CubeQuadrantQuads#setTexture(TextureAtlasSprite, TextureAtlasSprite, TextureAtlasSprite, TextureAtlasSprite, double, int)} instead.}
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
		
		dir1Face.setTexture(sideTexture, new double[]{8.0, 16.0, 16.0, 8.0}, vSides);
		dir2Face.setTexture(sideTexture, new double[]{0.0, 8.0, 8.0, 0.0}, vSides);
		
		if (topLid != null) {
			topLid.setTexture(endTexture,
					new double[]{uTop[0], (uTop[0] + uTop[1]) * 0.5, 8.0, (uTop[0] + uTop[3]) * 0.5},
					new double[]{vTop[0], (vTop[0] + vTop[1]) * 0.5, 8.0, (vTop[0] + vTop[3]) * 0.5});
		}
		if (bottomLid != null) {
			bottomLid.setTexture(endTexture,
					new double[]{(uTop[0] + uTop[3]) * 0.5, 8.0, (uTop[0] + uTop[1]) * 0.5, uTop[0]},
					new double[]{(vTop[0] + vTop[3]) * 0.5, 8.0, (vTop[0] + vTop[1]) * 0.5, vTop[0]});
		}
		return this;
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		dir1Face.render(renderer);
		dir2Face.render(renderer);
		if (topLid != null) topLid.render(renderer);
		if (bottomLid != null) bottomLid.render(renderer);
	}

	@Override
	public void render(WorldRenderer renderer, Double3 translate) {
		dir1Face.render(renderer, translate);
		dir2Face.render(renderer, translate);
		if (topLid != null) topLid.render(renderer, translate);
		if (bottomLid != null) bottomLid.render(renderer, translate);
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
		dir1Face.transform(rotation);
		dir2Face.transform(rotation);
		topLid.transform(rotation);
		bottomLid.transform(rotation);
		return this;
	}
}
