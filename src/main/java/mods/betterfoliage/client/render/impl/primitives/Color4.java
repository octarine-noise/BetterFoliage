package mods.betterfoliage.client.render.impl.primitives;

import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Immutable color object with ARGB components;
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class Color4 {

    public static final Color4 opaqueWhite = Color4.fromARGB(255, 255, 255, 255);
    public static final Color4 transparentWhite = Color4.fromARGB(0, 255, 255, 255);
    public static final Color4 opaqueBlack = Color4.fromARGB(255, 0, 0, 0);
    
    public int R, G, B, A;
    
    private Color4() {}
    
    private Color4(Color4 orig) {
        this.A = orig.A;
        this.R = orig.R;
        this.G = orig.G;
        this.B = orig.B;
    }
    
    /** 
     * @param color 4-byte encoded color value, order is ARGB from most to least significant bits
     * @return
     */
    public static Color4 fromARGB(int color) {
        Color4 result = new Color4();
        result.A = color >> 24 & 255;
        result.R = color >> 16 & 255;
        result.G = color >> 8 & 255;
        result.B = color & 255;
        return result;
    }
    
    public static Color4 fromARGB(int A, int R, int G, int B) {
        Color4 result = new Color4();
        result.A = A;
        result.R = R;
        result.G = G;
        result.B = B;
        return result;
    }
    
    /**
     * @return color with alpha component of 255
     */
    public Color4 opaque() {
        Color4 result = new Color4(this);
        result.A = 255;
        return result;
    }
    
    /** Multiply each component with a scalar value
     * @param scale 
     * @return
     */
    public Color4 multiply(float scale) {
        int scaleI = MathHelper.floor_float(scale * 255.0f);
        return fromARGB(A, R * scaleI / 255, G * scaleI / 255, B * scaleI / 255);
    }
    
}
