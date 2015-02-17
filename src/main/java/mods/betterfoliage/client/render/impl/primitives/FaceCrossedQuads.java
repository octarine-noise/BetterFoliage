package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Set of 4 quads (2 double-sided) centered on a block face
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class FaceCrossedQuads implements IQuadCollection {
    
    /** Quad with its normal pointing to the top-right of the block face */
    public DynamicQuad TR;
    
    /** Quad with its normal pointing to the top-left of the block face */
    public DynamicQuad TL;
    
    /** Quad with its normal pointing to the bottom-left of the block face */
    public DynamicQuad BL;
    
    /** Quad with its normal pointing to the bottom-right of the block face */
    public DynamicQuad BR;
    
    EnumFacing facing;
    
    private FaceCrossedQuads() {}
    
    /** Create quads with their base point perturbed along the block face.
     * @param base base point
     * @param facing face direction
     * @param perturb perturbation to apply (should be in plane XZ)
     * @param halfWidth half the quad edge length in face-parallel direction
     * @param halfHeight half the quad edge length in face-normal direction 
     * @return
     */
    public static FaceCrossedQuads createTranslated(Double3 base, EnumFacing facing, Double3 perturb, double halfWidth, double halfHeight) {
        FaceCrossedQuads result = new FaceCrossedQuads();
        Double3 faceN = new Double3(facing).scale(halfHeight);
        Double3 faceTR = new Double3(DynamicQuad.faceRight[facing.ordinal()]).add(new Double3(DynamicQuad.faceTop[facing.ordinal()])).scale(halfWidth);
        Double3 faceTL = new Double3(DynamicQuad.faceRight[facing.ordinal()]).inverse().add(new Double3(DynamicQuad.faceTop[facing.ordinal()])).scale(halfWidth);
        Double3 drawCenter = base.add(faceN).add(new Double3(DynamicQuad.faceRight[facing.ordinal()]).scale(perturb.x)).add(new Double3(DynamicQuad.faceTop[facing.ordinal()]).scale(perturb.z));
        
        result.BR = DynamicQuad.createParallelogram(drawCenter, faceTR, faceN);
        result.TL = DynamicQuad.createParallelogram(drawCenter, faceTR.inverse(), faceN);
        result.TR = DynamicQuad.createParallelogram(drawCenter, faceTL, faceN);
        result.BL = DynamicQuad.createParallelogram(drawCenter, faceTL.inverse(), faceN);
        result.facing = facing;
        return result;
    }
    
    public FaceCrossedQuads setTexture(TextureAtlasSprite texture, int uvRot) {
        TR.setTexture(texture, uvRot);
        TL.setTexture(texture, uvRot);
        BL.setTexture(texture, uvRot);
        BR.setTexture(texture, uvRot);
        return this;
    }
    
    public FaceCrossedQuads setBrightness(BlockShadingData shadingData) {
        int brTR = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()], false);
        int brTL = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()], false);
        int brBL = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false);
        int brBR = shadingData.getBrightness(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false);
        
        BR.setBrightness(brTR, brBL, brBL, brTR);
        TL.setBrightness(brBL, brTR, brTR, brBL);
        TR.setBrightness(brTL, brBR, brBR, brTL);
        BL.setBrightness(brBR, brTL, brTL, brBR);
        return this;
    }
    
    public FaceCrossedQuads setColor(BlockShadingData shadingData, Color4 color) {
        if (shadingData.useAO) {
            Color4 colTR = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()], false));
            Color4 colTL = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()], false));
            Color4 colBL = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()].getOpposite(), DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false));
            Color4 colBR = color.multiply(shadingData.getColorMultiplier(facing, DynamicQuad.faceRight[facing.ordinal()], DynamicQuad.faceTop[facing.ordinal()].getOpposite(), false));
            
            BR.setColor(colTR, colBL, colBL, colTR);
            TL.setColor(colBL, colTR, colTR, colBL);
            TR.setColor(colTL, colBR, colBR, colTL);
            BL.setColor(colBR, colTL, colTL, colBR);
        } else {
            TR.setColor(color);
            TL.setColor(color);
            BL.setColor(color);
            BR.setColor(color);
        }
        return this;
    }
    
    public void render(WorldRenderer renderer) {
        TR.render(renderer);
        TL.render(renderer);
        BL.render(renderer);
        BR.render(renderer);
    }
}
