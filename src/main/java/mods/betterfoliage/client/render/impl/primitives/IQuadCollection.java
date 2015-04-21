package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.IShadingData;
import mods.betterfoliage.client.render.Rotation;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IQuadCollection {

    /** Set the texture to use for quads in this collection
     * @param texture texture to use
     * @param uvRot rotate texture by +90deg this many times
     * @return quad collection with texture applied
     */
    public IQuadCollection setTexture(TextureAtlasSprite texture, int uvRot);
    
    /** Set the brightness of all vertices in this collection
     * @param shadingData {@link BlockShadingData} of the block these quads are for
     * @return quad collection with brightness applied
     */
    public IQuadCollection setBrightness(IShadingData shadingData);
    
    /** Set the color of all vertices in this collection
     * @param shadingData {@link BlockShadingData} of the block these quads are for
     * @param color color of vertices
     * @return quad collection with color applied
     */
    public IQuadCollection setColor(IShadingData shadingData, Color4 color);
    
    /** Render these quads with the given renderer
     * @param renderer the renderer
     */
    public void render(WorldRenderer renderer);
    
    /** Render these quads with the given renderer and translation
     * @param renderer the renderer
     * @param translate translate vertices with this vector
     */
    public void render(WorldRenderer renderer, Double3 translate);
    
    /** Apply a {@link Rotation} to the quads in this collection
     * @param rotation
     * @return
     */
    public IQuadCollection transform(Rotation rotation);
}
