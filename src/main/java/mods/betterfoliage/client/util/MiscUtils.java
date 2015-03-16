package mods.betterfoliage.client.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableSet;

@SideOnly(Side.CLIENT)
public class MiscUtils {

    /** Hide constructor */
    private MiscUtils() {}

    public static void stripTooltipDefaultText(List<String> tooltip) {
        boolean defaultRows = false;
        Iterator<String> iter = tooltip.iterator();
        while(iter.hasNext()) {
            defaultRows |= iter.next().startsWith(EnumChatFormatting.AQUA.toString());
            if (defaultRows) iter.remove();
        }
    }
    
    @SuppressWarnings("unchecked")
	public static boolean hasState(IBlockState state, String stateName, Comparable<?> stateValue) {
    	for (Map.Entry<IProperty, Comparable<?>> entry : (ImmutableSet<Map.Entry<IProperty, Comparable<?>>>) state.getProperties().entrySet()) {
    		if (entry.getKey().getName().equals(stateName) && entry.getValue().equals(stateValue)) return true;
    	}
    	return false;
    }
    
    @SuppressWarnings("unchecked")
	public static Object getState(IBlockState state, String stateName) {
    	for (Map.Entry<IProperty, Comparable<?>> entry : (ImmutableSet<Map.Entry<IProperty, Comparable<?>>>) state.getProperties().entrySet()) {
    		if (entry.getKey().getName().equals(stateName)) return entry.getValue();
    	}
    	return null;
    }
}
