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
public class SimpleOrientedQuad implements IQuadCollection {

    public DynamicQuad quad;
    
    EnumFacing facing;
    
    private SimpleOrientedQuad() {}
    
    public static SimpleOrientedQuad create(Double3 center, EnumFacing facing, double halfSize) {
        SimpleOrientedQuad result = new SimpleOrientedQuad();
        Double3 horz1 = new Double3(DynamicQuad.faceRight[facing.ordinal()]).scale(halfSize);
        Double3 horz2 = new Double3(DynamicQuad.faceTop[facing.ordinal()]).scale(halfSize);
        result.quad = DynamicQuad.createParallelogramCentered(center, horz1, horz2);
        result.facing = facing;
        return result;
    }

    @Override
    public IQuadCollection setTexture(TextureAtlasSprite texture, int uvRot) {
        quad.setTexture(texture, uvRot);
        return this;
    }

    @Override
    public IQuadCollection setBrightness(IShadingData shadingData) {
        int brTR = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()], false);
        int brTL = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()], false);
        int brBL = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false);
        int brBR = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false);
        quad.setBrightness(brTR, brTL, brBL, brBR);
        return this;
    }

    @Override
    public IQuadCollection setColor(IShadingData shadingData, Color4 color) {
        if (shadingData.shouldUseAO()) {
            Color4 colTR = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()], false));
            Color4 colTL = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()], false));
            Color4 colBL = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false));
            Color4 colBR = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false));
            
            quad.setColor(colTR, colTL, colBL, colBR);
        } else {
            quad.setColor(color);
        }
        return this;
    }

    @Override
    public void render(WorldRenderer renderer) {
        quad.render(renderer);
    }

    @Override
    public void render(WorldRenderer renderer, Double3 translate) {
        quad.render(renderer, translate);
    }
    
	@Override
	public IQuadCollection transform(Rotation rotation) {
		quad.transform(rotation);
		return this;
	}
    
}
