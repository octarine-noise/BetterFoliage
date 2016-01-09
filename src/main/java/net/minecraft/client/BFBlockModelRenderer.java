package net.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.EnumFacing;

import java.util.BitSet;

/**
 * FFS why isn't this public static...
 */
public class BFBlockModelRenderer extends BlockModelRenderer {
    public class BFAmbientOcclusionFace extends BlockModelRenderer.AmbientOcclusionFace {}

    private static BFBlockModelRenderer INSTANCE = new BFBlockModelRenderer();

    public static BFAmbientOcclusionFace getVanillaAoObject() {
        return INSTANCE.new BFAmbientOcclusionFace();
    }

    public static void fillQuadBounds2(Block blockIn, int[] vertexData, EnumFacing facingIn, float[] quadBounds, BitSet boundsFlags) {
        INSTANCE.fillQuadBounds(blockIn, vertexData, facingIn, quadBounds, boundsFlags);
    }

}
