package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.Rotation;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DynamicQuad {

    /** Designated "top" direction of a face */
    protected static EnumFacing[] faceTop = new EnumFacing[] {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP, EnumFacing.UP};

    /** Designated "right" direction of a face */
    protected static EnumFacing[] faceRight = new EnumFacing[] {EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH};

    /** Corner U values */
    protected static double[] uValues = new double[] {0.0, 16.0, 16.0, 0.0};
    
    /** Corner V values */
    protected static double[] vValues = new double[] {0.0, 0.0, 16.0, 16.0};
    
    protected final double[] x = new double[4];
    protected final double[] y = new double[4];
    protected final double[] z = new double[4];
    protected final Color4[] color = new Color4[4];
    protected final int[] brightness = new int[4];
    protected final double[] u = new double[4];
    protected final double[] v = new double[4];
    
    private DynamicQuad() {}
    
    /** Create quad forming a parallelogram.
     * @param center coordinates of midpoint of quad
     * @param vec1 vector from midpoint of quad to midpoint of "right" edge
     * @param vec2 vector from midpoint of quad to midpoint of "top" edge
     * @return quad data
     */
    public static DynamicQuad createParallelogramCentered(Double3 center, Double3 vec1, Double3 vec2) {
        DynamicQuad result = new DynamicQuad();
        result.putRawXYZ(center.add(vec1).add(vec2), 0);
        result.putRawXYZ(center.sub(vec1).add(vec2), 1);
        result.putRawXYZ(center.sub(vec1).sub(vec2), 2);
        result.putRawXYZ(center.add(vec1).sub(vec2), 3);
        return result;
    }
    
    /** Create quad forming a parallelogram.
     * @param center coordinates of midpoint of quad
     * @param vec1 vector from midpoint of quad to midpoint of "right" edge
     * @param vec2 vector from midpoint of quad to midpoint of "top" edge
     * @return quad data
     */
    public static DynamicQuad createParallelogramExtruded(Double3 vert1, Double3 vert2, Double3 extrude) {
        DynamicQuad result = new DynamicQuad();
        result.putRawXYZ(vert1, 0);
        result.putRawXYZ(vert2, 1);
        result.putRawXYZ(vert2.add(extrude), 2);
        result.putRawXYZ(vert1.add(extrude), 3);
        return result;
    }
    
    public static DynamicQuad createFromVertices(Double3 vert1, Double3 vert2, Double3 vert3, Double3 vert4) {
    	DynamicQuad result = new DynamicQuad();
        result.putRawXYZ(vert1, 0);
        result.putRawXYZ(vert2, 1);
        result.putRawXYZ(vert3, 2);
        result.putRawXYZ(vert4, 3);
        return result;
    }
    
    /** Set vertex XYZ coordinates
     * @param pos coordinates
     * @param index index of vertex. Accepted range of (0...3) is not checked!
     */
    protected void putRawXYZ(Double3 pos, int index) {
        x[index] = pos.x;
        y[index] = pos.y;
        z[index] = pos.z;
    }
    
    
    /** Set vertex UV coordinates
     * @param texture texture of quad
     * @param uvRot rotate UV coords by (+90deg) * uvRot
     * @return this for method chaining
     */
    public DynamicQuad setTexture(TextureAtlasSprite texture, int uvRot) {
        for (int idx = 0; idx < 4; idx++) {
            u[idx] = texture.getInterpolatedU(uValues[(uvRot + idx) & 3]);
            v[idx] = texture.getInterpolatedV(vValues[(uvRot + idx) & 3]);
        }
        return this;
    }
    
    public DynamicQuad setTexture(TextureAtlasSprite texture, double[] uCoords, double[] vCoords) {
    	for (int idx = 0; idx < 4; idx++) {
    		u[idx] = texture.getInterpolatedU(uCoords[idx]);
    		v[idx] = texture.getInterpolatedV(vCoords[idx]);
    	}
    	return this;
    }
    
    /** Set vertex colors
     * @param color
     * @return this for method chaining
     */
    public DynamicQuad setColor(Color4 color) {
        for (int idx = 0; idx < 4; idx++) this.color[idx] = color;
        return this;
    }
    
    /** Set vertex colors
     * @param col0 color for vertex 0
     * @param col1 color for vertex 1
     * @param col2 color for vertex 2
     * @param col3 color for vertex 3
     * @return this for method chaining
     */
    public DynamicQuad setColor(Color4 col0, Color4 col1, Color4 col2, Color4 col3) {
        color[0] = col0;
        color[1] = col1;
        color[2] = col2;
        color[3] = col3;
        return this;
    }
    
    /** Set vertex brightnesses
     * @param brightness
     * @return this for method chaining
     */
    public DynamicQuad setBrightness(int brightness) {
        for (int idx = 0; idx < 4; idx++) this.brightness[idx] = brightness;
        return this;
    }
    
    /** Set vertex brightnesses
     * @param br0 brightness for vertex 0
     * @param br1 brightness for vertex 1
     * @param br2 brightness for vertex 2
     * @param br3 brightness for vertex 3
     * @return
     */
    public DynamicQuad setBrightness(int br0, int br1, int br2, int br3) {
        brightness[0] = br0;
        brightness[1] = br1;
        brightness[2] = br2;
        brightness[3] = br3;
        return this;
    }
    
    public void transform(Rotation rotation) {
    	for (int idx = 0; idx < 4; idx++) {
        	Double3 orig = new Double3(x[idx], y[idx], z[idx]);
        	putRawXYZ(rotation.transform(orig), idx);
    	}
    }

    /** Render vertex data with renderer
     * @param renderer
     */
    public void render(WorldRenderer renderer) {
        for (int idx = 0; idx < 4; idx++) {
            Color4 col = color[idx];
            renderer.setColorRGBA(col.R, col.G, col.B, col.A);
            renderer.setBrightness(brightness[idx]);
            renderer.setTextureUV(u[idx], v[idx]);
            renderer.addVertex(x[idx], y[idx], z[idx]);
        }
    }
    
    /** Render vertex data with renderer and offset
     * @param renderer
     * @param offset
     */
    public void render(WorldRenderer renderer, Double3 offset) {
        for (int idx = 0; idx < 4; idx++) {
            Color4 col = color[idx];
            renderer.setColorRGBA(col.R, col.G, col.B, col.A);
            renderer.setBrightness(brightness[idx]);
            renderer.setTextureUV(u[idx], v[idx]);
            renderer.addVertex(x[idx] + offset.x, y[idx] + offset.y, z[idx] + offset.z);
        }
    }
    
}