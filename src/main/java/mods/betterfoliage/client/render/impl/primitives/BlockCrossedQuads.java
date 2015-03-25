package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Set of 4 vertical quads (2 double-sided) centered on a block. 
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class BlockCrossedQuads implements IQuadCollection {

    public static boolean useMax = true;
    
    /** Primary direction (axis0). Axis 1 & 2 point to the "right" and "top" of the face towards axis0 */
    public EnumFacing axisMain;
    
    /** Quad with its primary direction pointing to axis1+, axis2+ */
    public DynamicQuad PP;
    
    /** Quad with its primary direction pointing to axis1-, axis2+ */
    public DynamicQuad NP;
    
    /** Quad with its primary direction pointing to axis1+, axis2- */
    public DynamicQuad PN;
    
    /** Quad with its primary direction pointing to axis1-, axis2- */
    public DynamicQuad NN;
    
    private BlockCrossedQuads() {}
    
    /** Create quads with their primary directions perturbed, but at the exact block centerpoint.
     * @param blockCenter center of quads
     * @param perturb1 additive perturbation to PP vector
     * @param perturb2 additive perturbation to PN vector
     * @param halfSize half side length
     * @return quads
     */
    public static BlockCrossedQuads createSkewed(EnumFacing axisMain, Double3 blockCenter, Double3 perturb1, Double3 perturb2, double halfSize) {
        BlockCrossedQuads result = new BlockCrossedQuads();
        result.axisMain = axisMain;
        
		Double3 axis1 = new Double3(DynamicQuad.faceRight[axisMain.ordinal()]);
		Double3 axis2 = new Double3(DynamicQuad.faceTop[axisMain.ordinal()]);
		
		Double3 horz1 = axis1.add(axis2).scale(halfSize).add(axis1.scale(perturb1.x)).add(axis2.scale(perturb1.z)).add(new Double3(axisMain).scale(perturb1.y));
		Double3 horz2 = axis1.sub(axis2).scale(halfSize).add(axis1.scale(perturb2.x)).add(axis2.scale(perturb2.z)).add(new Double3(axisMain).scale(perturb2.y));
		Double3 vert = new Double3(axisMain).scale(halfSize * 1.41);
        
        result.PP = DynamicQuad.createParallelogramCentered(blockCenter, horz1, vert);
        result.NN = DynamicQuad.createParallelogramCentered(blockCenter, horz1.inverse(), vert);
        result.PN = DynamicQuad.createParallelogramCentered(blockCenter, horz2, vert);
        result.NP = DynamicQuad.createParallelogramCentered(blockCenter, horz2.inverse(), vert);
        
        return result;
    }
    
    /** Create quads with their centerpoint perturbed, but at exact 45deg angles.
     * @param blockCenter center of block
     * @param perturb additive perturbation to centerpoint
     * @param halfSize half side length
     * @return quads
     */
    public static BlockCrossedQuads createTranslated(EnumFacing facing, Double3 blockCenter, Double3 perturb, double halfSize) {
        BlockCrossedQuads result = new BlockCrossedQuads();
        result.axisMain = facing;
        
		Double3 axis1 = new Double3(DynamicQuad.faceRight[facing.ordinal()]);
		Double3 axis2 = new Double3(DynamicQuad.faceTop[facing.ordinal()]);
		
        Double3 drawCenter = blockCenter.add(perturb);
        Double3 horz1 = axis1.add(axis2).scale(halfSize);
        Double3 horz2 = axis1.sub(axis2).scale(halfSize);
        Double3 vert = new Double3(facing).scale(halfSize * 1.41);
        
        result.PP = DynamicQuad.createParallelogramCentered(drawCenter, horz1, vert);
        result.NN = DynamicQuad.createParallelogramCentered(drawCenter, horz1.inverse(), vert);
        result.PN = DynamicQuad.createParallelogramCentered(drawCenter, horz2, vert);
        result.NP = DynamicQuad.createParallelogramCentered(drawCenter, horz2.inverse(), vert);
        
        return result;
    }
    
    public IQuadCollection setTexture(TextureAtlasSprite texture, int uvRot) {
        PP.setTexture(texture, uvRot);
        NN.setTexture(texture, uvRot);
        PN.setTexture(texture, uvRot);
        NP.setTexture(texture, uvRot);
        return this;
    }
    
    public IQuadCollection setBrightness(BlockShadingData shadingData) {
		EnumFacing axis1P = DynamicQuad.faceRight[axisMain.ordinal()];
		EnumFacing axis2P = DynamicQuad.faceTop[axisMain.ordinal()];
		EnumFacing axis1N = axis1P.getOpposite();
		EnumFacing axis2N = axis2P.getOpposite();
		EnumFacing axis0N = axisMain.getOpposite();
    	
        PP.setBrightness(shadingData.getBrightness(axis2P, axisMain, axis1P, useMax),
                         shadingData.getBrightness(axis1N, axisMain, axis2N, useMax),
                         shadingData.getBrightness(axis1N, axis0N, axis2N, useMax),
                         shadingData.getBrightness(axis2P, axis0N, axis1P, useMax));
        
        NN.setBrightness(shadingData.getBrightness(axis1N, axisMain, axis2N, useMax),
                         shadingData.getBrightness(axis2P, axisMain, axis1P, useMax),
                         shadingData.getBrightness(axis2P, axis0N, axis1P, useMax),
                         shadingData.getBrightness(axis1N, axis0N, axis2N, useMax));
        
        PN.setBrightness(shadingData.getBrightness(axis1P, axisMain, axis2N, useMax),
                         shadingData.getBrightness(axis2P, axisMain, axis1N, useMax),
                         shadingData.getBrightness(axis2P, axis0N, axis1N, useMax),
                         shadingData.getBrightness(axis1P, axis0N, axis2N, useMax));
        
        NP.setBrightness(shadingData.getBrightness(axis2P, axisMain, axis1N, useMax),
                         shadingData.getBrightness(axis1P, axisMain, axis2N, useMax),
                         shadingData.getBrightness(axis1P, axis0N, axis2N, useMax),
                         shadingData.getBrightness(axis2P, axis0N, axis1N, useMax));
        return this;
    }

    public IQuadCollection setColor(BlockShadingData shadingData, Color4 color) {
        if (shadingData.useAO) {
    		EnumFacing axis1P = DynamicQuad.faceRight[axisMain.ordinal()];
    		EnumFacing axis2P = DynamicQuad.faceTop[axisMain.ordinal()];
    		EnumFacing axis1N = axis1P.getOpposite();
    		EnumFacing axis2N = axis2P.getOpposite();
    		EnumFacing axis0N = axisMain.getOpposite();
    		
            PP.setColor(color.multiply(shadingData.getColorMultiplier(axis2P, axisMain, axis1P, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1N, axisMain, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1N, axis0N, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axis0N, axis1P, useMax)));
            
            NN.setColor(color.multiply(shadingData.getColorMultiplier(axis1N, axisMain, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axisMain, axis1P, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axis0N, axis1P, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1N, axis0N, axis2N, useMax)));
            
            PN.setColor(color.multiply(shadingData.getColorMultiplier(axis1P, axisMain, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axisMain, axis1N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axis0N, axis1N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1P, axis0N, axis2N, useMax)));
            
            NP.setColor(color.multiply(shadingData.getColorMultiplier(axis2P, axisMain, axis1N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1P, axisMain, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis1P, axis0N, axis2N, useMax)),
                        color.multiply(shadingData.getColorMultiplier(axis2P, axis0N, axis1N, useMax)));
        } else {
            PP.setColor(color);
            NN.setColor(color);
            PN.setColor(color);
            NP.setColor(color);
        }
        return this;
    }
    
    public void render(WorldRenderer renderer) {
        PP.render(renderer);
        NN.render(renderer);
        PN.render(renderer);
        NP.render(renderer);
    }
    
}
