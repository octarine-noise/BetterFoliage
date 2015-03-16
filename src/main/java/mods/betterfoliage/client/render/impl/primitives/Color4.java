package mods.betterfoliage.client.render.impl.primitives;

import java.awt.Color;

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
    
    public static Color4 average(Color4 col1, Color4 col2) {
    	Color4 result = new Color4();
    	result.A = (col1.A + col2.A) / 2;
    	result.R = (col1.R + col2.R) / 2;
    	result.G = (col1.G + col2.G) / 2;
    	result.B = (col1.B + col2.B) / 2;
    	return result;
    }
    
    public static Color4 average(Color4 col1, Color4 col2, Color4 col3, Color4 col4) {
    	Color4 result = new Color4();
    	result.A = (col1.A + col2.A + col3.A + col4.A) / 4;
    	result.R = (col1.R + col2.R + col3.R + col4.R) / 4;
    	result.G = (col1.G + col2.G + col3.G + col4.G) / 4;
    	result.B = (col1.B + col2.B + col3.B + col4.B) / 4;
    	return result;
    }
    
    public float getSaturation() {
    	return Color.RGBtoHSB(R, G, B, null)[1];
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
    
    public Color4 withHSVBrightness(float value) {
    	float[] hsvValues = Color.RGBtoHSB(R, G, B, null);
    	return fromARGB(A | Color.HSBtoRGB(hsvValues[0], hsvValues[1], value));
    }
}
