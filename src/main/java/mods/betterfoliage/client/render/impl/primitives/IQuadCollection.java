package mods.betterfoliage.client.render.impl.primitives;

import mods.betterfoliage.client.render.BlockShadingData;
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
    public IQuadCollection setBrightness(BlockShadingData shadingData);
    
    /** Set the color of all vertices in this collection
     * @param shadingData {@link BlockShadingData} of the block these quads are for
     * @param color color of vertices
     * @return quad collection with color applied
     */
    public IQuadCollection setColor(BlockShadingData shadingData, Color4 color);
    
    /** Render these quads with the given renderer
     * @param renderer the renderer
     */
    public void render(WorldRenderer renderer);
}
