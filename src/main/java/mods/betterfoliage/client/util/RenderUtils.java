package mods.betterfoliage.client.util;

import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils {

    /** Hide constructor */
    private RenderUtils() {}


    public static void stripTooltipDefaultText(List<String> tooltip) {
        boolean defaultRows = false;
        Iterator<String> iter = tooltip.iterator();
        while(iter.hasNext()) {
            defaultRows |= iter.next().startsWith(EnumChatFormatting.AQUA.toString());
            if (defaultRows) iter.remove();
        }
    }
    
    public static int getColorI(int r, int g, int b, int a) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            return a << 24 | b << 16 | g << 8 | r;
        }
        else
        {
            return r << 24 | g << 16 | b << 8 | a;
        }
    }
    
    public static int getColorOpaque(int color) {
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            return color | 0xff000000;
        }
        else
        {
            return color | 0xff;
        }
    }
    
    public static int getMaxInt(int... nums) {
        int result = Integer.MIN_VALUE;
        for (int num : nums) if (num > result) result = num;
        return result;
    }
    
    public static float getMaxFloat(float... nums) {
        float result = Float.MIN_VALUE;
        for (float num : nums) if (num > result) result = num;
        return result;
    }
}
