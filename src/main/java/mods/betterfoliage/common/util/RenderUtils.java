package mods.betterfoliage.common.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;


public class RenderUtils {

    /** Hide constructor */
    private RenderUtils() {}

    /** Retrieve a specific rendering handler from the registry
     * @param renderType render type of block
     * @return {@link ISimpleBlockRenderingHandler} if defined, null otherwise
     */
    @SuppressWarnings("unchecked")
    public static ISimpleBlockRenderingHandler getRenderingHandler(int renderType) {
    	try {
    		Field field = RenderingRegistry.class.getDeclaredField("INSTANCE");
    		field.setAccessible(true);
    		RenderingRegistry inst = (RenderingRegistry) field.get(null);
    		field = RenderingRegistry.class.getDeclaredField("blockRenderers");
    		field.setAccessible(true);
    		return ((Map<Integer, ISimpleBlockRenderingHandler>) field.get(inst)).get(renderType);
    	} catch (Exception e) {
    		return null;
    	}
    }

    public static void stripTooltipDefaultText(List<String> tooltip) {
        boolean defaultRows = false;
        Iterator<String> iter = tooltip.iterator();
        while(iter.hasNext()) {
            if (iter.next().startsWith(EnumChatFormatting.AQUA.toString())) defaultRows = true;
            if (defaultRows) iter.remove();
        }
    }
    
}
