package mods.betterfoliage.client.render;

import java.util.BitSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BFAbstractRenderer.BFAmbientOcclusionFace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Shading data (both AO and simple brightness) for a single world block
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class BlockShadingData {

    /** AO data for all faces */
    public BFAmbientOcclusionFace[] aoFaces = new BFAmbientOcclusionFace[6];
    
    public BitSet shadingBitSet = new BitSet(3);
    
    /** Translucence for all faces */
    public boolean[] isTranslucent = new boolean[6];
    
    /** Mixed block brightness for all faces */
    public int[] mixedBrightness = new int[6];
    
    /** Currently using AO or not */
    public boolean useAO;

    /** Quick lookup: vertex index in {@link BFAmbientOcclusionFace} arrays for the block corner specified by 3 {@link EnumFacing} directions */
    public static int[][][] vertexIndexToFaces = new int[6][6][6];
    
    static {
        for (EnumFacing face : EnumFacing.values()) for (EnumFacing axis1 : EnumFacing.values()) for (EnumFacing axis2 : EnumFacing.values()) {
            vertexIndexToFaces[face.ordinal()][axis1.ordinal()][axis2.ordinal()] = BFAbstractRenderer.getAoIndexForFaces(face, axis1, axis2);
        }
    }
    
    public BlockShadingData(BFAbstractRenderer renderer) {
    	shadingBitSet.set(0);
        for (int j = 0; j < EnumFacing.values().length; ++j) aoFaces[j] = renderer.new BFAmbientOcclusionFace();
    }
    
    /** Calculate shading data for the given block & position.
     * @param blockAccessIn world instance
     * @param blockIn block
     * @param blockPosIn block position
     * @param useAO true for ambient occlusion data, false for basic block brightness 
     */
    public void update(IBlockAccess blockAccessIn, Block blockIn, BlockPos blockPosIn, boolean useAO) {
        this.useAO = useAO;
        if (useAO) {
            // update ambient occlusion data
            for (EnumFacing facing : EnumFacing.values()) {
                aoFaces[facing.ordinal()].updateVertexBrightness(blockAccessIn, blockIn, blockPosIn, facing, null, shadingBitSet);
                isTranslucent[facing.ordinal()] = blockAccessIn.getBlockState(blockPosIn).getBlock().isTranslucent();
            }
        } else {
            // update basic brightness data
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos facingPos = blockPosIn.offset(facing);
                mixedBrightness[facing.ordinal()] = blockAccessIn.getBlockState(facingPos).getBlock().getMixedBrightnessForBlock(blockAccessIn, facingPos);
                isTranslucent[facing.ordinal()] = blockAccessIn.getBlockState(blockPosIn).getBlock().isTranslucent();
            }
        }
    }
    
    /** Get the brightness value to use for a vertex in a given block corner. The corner is defined by its 3 worldspace directions (eg. TOP-SOUTH-WEST).
     * Since there are 3 faces meeting in a corner, the primary direction determines which value to use.
     * @param primary direction of face
     * @param secondary direction of corner on face
     * @param tertiary direction of corner on face
     * @param useMax if true, check the other 2 permutations and return the maximum value
     * @return brightness of block corner
     */
    public int getBrightness(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax) {
        if (useAO) {
            int pri = aoFaces[primary.ordinal()].vertexBrightness[ vertexIndexToFaces[primary.ordinal()][secondary.ordinal()][tertiary.ordinal()] ];
            if (!useMax) return pri;
            int sec = aoFaces[secondary.ordinal()].vertexBrightness[ vertexIndexToFaces[secondary.ordinal()][primary.ordinal()][tertiary.ordinal()] ];
            int ter = aoFaces[tertiary.ordinal()].vertexBrightness[ vertexIndexToFaces[tertiary.ordinal()][primary.ordinal()][secondary.ordinal()] ];
            return pri > sec && pri > ter ? pri : (sec > ter ? sec : ter);
        } else {
            int pri = mixedBrightness[primary.ordinal()];
            if (!useMax) return pri;
            int sec = mixedBrightness[secondary.ordinal()];
            int ter = mixedBrightness[tertiary.ordinal()];
            return pri > sec && pri > ter ? pri : (sec > ter ? sec : ter);
        }
    }
    
    /** Get the color multiplier to use for a vertex in a given block corner. The corner is defined by its 3 worldspace directions (eg. TOP-SOUTH-WEST).
     * Since there are 3 faces meeting in a corner, the primary direction determines which value to use.
     * @param primary direction of face
     * @param secondary direction of corner on face
     * @param tertiary direction of corner on face
     * @param useMax if true, check the other 2 permutations and return the maximum value
     * @return color multiplier of block corner
     */
    public float getColorMultiplier(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax) {
        float pri = aoFaces[primary.ordinal()].vertexColorMultiplier[ vertexIndexToFaces[primary.ordinal()][secondary.ordinal()][tertiary.ordinal()] ];
        if (!useMax) return pri;
        float sec = aoFaces[secondary.ordinal()].vertexColorMultiplier[ vertexIndexToFaces[secondary.ordinal()][primary.ordinal()][tertiary.ordinal()] ];
        float ter = aoFaces[tertiary.ordinal()].vertexColorMultiplier[ vertexIndexToFaces[tertiary.ordinal()][primary.ordinal()][secondary.ordinal()] ];
        return pri > sec && pri > ter ? pri : (sec > ter ? sec : ter);
    }
}
