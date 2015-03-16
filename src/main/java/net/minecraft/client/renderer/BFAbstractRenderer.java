package net.minecraft.client.renderer;

import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.misc.PerturbationSource;
import mods.betterfoliage.client.render.BlockShadingData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Base class for Better Foliage renderers. Resdides in the Minecraft package hierarchy because of inner class visibility reasons.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public abstract class BFAbstractRenderer extends BlockModelRenderer {

    /** Identical to {@link AmbientOcclusionFace} with <b>public</b> visibility.
     * @author octarine-noise
     */
    public class BFAmbientOcclusionFace extends AmbientOcclusionFace {}
    
    /** Shading data for the currently rendered block */
    public BlockShadingData shadingData = new BlockShadingData(this);
    
    /** Perturbation source */
    public PerturbationSource random = new PerturbationSource();
    
    /** Render feature for a block.
     * This method gets called for <i>every block</i>, so it needs to fail-fast if the block is not eligible for this feature. 
     * @param blockAccess world instance
     * @param blockState block state
     * @param pos position
     * @param worldRenderer renderer
     * @param useAO ambient occlusion should be used for the block
     * @return true if drawing took place
     */
    public abstract boolean renderFeatureForBlock(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO);
    
    /** Get the {@link AmbientOcclusionFace} array index for a corner vertex
     * @param face block face
     * @param axis1 1st cardinal direction of corner
     * @param axis2 2nd cardinal direction of corner
     * @return
     */
    public static int getAoIndexForFaces(EnumFacing face, EnumFacing axis1, EnumFacing axis2) {
        EnumNeighborInfo neighborInfo = EnumNeighborInfo.getNeighbourInfo(face);
        BlockModelRenderer.VertexTranslations vertexTrans = VertexTranslations.getVertexTranslations(face);
        
        boolean top = false;
        boolean left = false;
        
        if (axis1 == neighborInfo.field_178276_g[0] || axis1 == neighborInfo.field_178276_g[0]) top = true;
        if (axis1 == neighborInfo.field_178276_g[2] || axis1 == neighborInfo.field_178276_g[2]) left = true;
        
        if (top && !left) return vertexTrans.field_178191_g;
        if (top && left) return vertexTrans.field_178200_h;
        if (!top && left) return vertexTrans.field_178201_i;
        if (!top && !left) return vertexTrans.field_178198_j;
        return 0;
    }
    
    /** Get a chaotic but deterministic value depending on block position.
     * @param pos block position
     * @param seed additional random seed
     * @return semirandom value
     */
    protected int getSemiRandomFromPos(BlockPos pos, int seed) {
        long lx = pos.getX();
        long ly = pos.getY();
        long lz = pos.getZ();
        long value = (lx * lx + ly * ly + lz * lz + lx * ly + ly * lz + lz * lx + seed * seed) & 63;
        value = (3 * lx * value + 5 * ly * value + 7 * lz * value + 11 * seed) & 63;
        return (int) value;
    }
    
    /** Get the base coordinates for the chunk the given block is in
     * @param pos block position
     * @return absolute world coordinates of chunk origin
     */
    protected Double3 getChunkBase(BlockPos pos) {
        int cX = pos.getX() >= 0 ? pos.getX() / 16 : (pos.getX() + 1) / 16 - 1;
        int cY = pos.getY() / 16;
        int cZ = pos.getZ() >= 0 ? pos.getZ() / 16 : (pos.getZ() + 1) / 16 - 1;
        return new Double3(cX * 16, cY * 16, cZ * 16);
    }
    
}
