package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.common.util.Double3;
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
    
    /** Quad with its normal pointing to north-east */
    public DynamicQuad NE;
    
    /** Quad with its normal pointing to north-west */
    public DynamicQuad NW;
    
    /** Quad with its normal pointing to south-east */
    public DynamicQuad SE;
    
    /** Quad with its normal pointing to south-west */
    public DynamicQuad SW;
    
    private BlockCrossedQuads() {}
    
    /** Create quads with their primary directions perturbed, but at the exact block centerpoint.
     * @param blockCenter center of quads
     * @param perturb1 additive perturbation to SE vector
     * @param perturb2 additive perturbation to NE vector
     * @param halfSize half side length
     * @return quads
     */
    public static BlockCrossedQuads createSkewed(Double3 blockCenter, Double3 perturb1, Double3 perturb2, double halfSize) {
        BlockCrossedQuads result = new BlockCrossedQuads();
        Double3 horz1 = new Double3(halfSize, 0.0, halfSize).add(perturb1);
        Double3 horz2 = new Double3(halfSize, 0.0, -halfSize).add(perturb2);
        Double3 vert = new Double3(0.0, halfSize * 1.41, 0.0);
        
        result.SW = DynamicQuad.createParallelogram(blockCenter, horz1, vert);
        result.NE = DynamicQuad.createParallelogram(blockCenter, horz1.inverse(), vert);
        result.SE = DynamicQuad.createParallelogram(blockCenter, horz2, vert);
        result.NW = DynamicQuad.createParallelogram(blockCenter, horz2.inverse(), vert);
        
        return result;
    }
    
    /** Create quads with their centerpoint perturbed, but at exact 45deg angles.
     * @param blockCenter center of block
     * @param perturb additive perturbation to centerpoint
     * @param halfSize half side length
     * @return quads
     */
    public static BlockCrossedQuads createTranslated(Double3 blockCenter, Double3 perturb, double halfSize) {
        BlockCrossedQuads result = new BlockCrossedQuads();
        Double3 drawCenter = blockCenter.add(perturb);
        Double3 horz1 = new Double3(halfSize, 0.0, halfSize);
        Double3 horz2 = new Double3(halfSize, 0.0, -halfSize);
        Double3 vert = new Double3(0.0, halfSize * 1.41, 0.0);
        
        result.SW = DynamicQuad.createParallelogram(drawCenter, horz1, vert);
        result.NE = DynamicQuad.createParallelogram(drawCenter, horz1.inverse(), vert);
        result.SE = DynamicQuad.createParallelogram(drawCenter, horz2, vert);
        result.NW = DynamicQuad.createParallelogram(drawCenter, horz2.inverse(), vert);
        
        return result;
    }
    
    public IQuadCollection setTexture(TextureAtlasSprite texture, int uvRot) {
        NE.setTexture(texture, uvRot);
        NW.setTexture(texture, uvRot);
        SE.setTexture(texture, uvRot);
        SW.setTexture(texture, uvRot);
        return this;
    }
    
    public IQuadCollection setBrightness(BlockShadingData shadingData) {
        NE.setBrightness(shadingData.getBrightness(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.WEST, useMax),
                         shadingData.getBrightness(EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH, useMax),
                         shadingData.getBrightness(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.SOUTH, useMax),
                         shadingData.getBrightness(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.WEST, useMax));
        
        NW.setBrightness(shadingData.getBrightness(EnumFacing.WEST, EnumFacing.UP, EnumFacing.SOUTH, useMax),
                         shadingData.getBrightness(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.EAST, useMax),
                         shadingData.getBrightness(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.EAST, useMax),
                         shadingData.getBrightness(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.SOUTH, useMax));
        
        SE.setBrightness(shadingData.getBrightness(EnumFacing.EAST, EnumFacing.UP, EnumFacing.NORTH, useMax),
                         shadingData.getBrightness(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.WEST, useMax),
                         shadingData.getBrightness(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.WEST, useMax),
                         shadingData.getBrightness(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.NORTH, useMax));
        
        SW.setBrightness(shadingData.getBrightness(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.EAST, useMax),
                         shadingData.getBrightness(EnumFacing.WEST, EnumFacing.UP, EnumFacing.NORTH, useMax),
                         shadingData.getBrightness(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.NORTH, useMax),
                         shadingData.getBrightness(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.EAST, useMax));
        return this;
    }

    public IQuadCollection setColor(BlockShadingData shadingData, Color4 color) {
        if (shadingData.useAO) {
            NE.setColor(color.multiply(shadingData.getColorMultiplier(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.WEST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.SOUTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.WEST, useMax)));
            
            NW.setColor(color.multiply(shadingData.getColorMultiplier(EnumFacing.WEST, EnumFacing.UP, EnumFacing.SOUTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.NORTH, EnumFacing.UP, EnumFacing.EAST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.NORTH, EnumFacing.DOWN, EnumFacing.EAST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.SOUTH, useMax)));
            
            SE.setColor(color.multiply(shadingData.getColorMultiplier(EnumFacing.EAST, EnumFacing.UP, EnumFacing.NORTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.WEST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.WEST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.NORTH, useMax)));
            
            SW.setColor(color.multiply(shadingData.getColorMultiplier(EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.EAST, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.WEST, EnumFacing.UP, EnumFacing.NORTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.WEST, EnumFacing.DOWN, EnumFacing.NORTH, useMax)),
                        color.multiply(shadingData.getColorMultiplier(EnumFacing.SOUTH, EnumFacing.DOWN, EnumFacing.EAST, useMax)));
        } else {
            NE.setColor(color);
            NW.setColor(color);
            SE.setColor(color);
            SW.setColor(color);
        }
        return this;
    }
    
    public void render(WorldRenderer renderer) {
        NE.render(renderer);
        NW.render(renderer);
        SE.render(renderer);
        SW.render(renderer);
    }
    
}
