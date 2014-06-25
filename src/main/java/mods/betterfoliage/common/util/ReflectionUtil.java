package mods.betterfoliage.common.util;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ReflectionUtil {

	private ReflectionUtil() {}
	
	@SuppressWarnings("unchecked")
	public static Map<String, IResourceManager> getDomainResourceManagers() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		Map<String, IResourceManager> result = getField(manager, DeobfNames.SRRM_DRM_MCP, Map.class);
		if (result == null) result = getField(manager, DeobfNames.SRRM_DRM_SRGNAME, Map.class);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getField(Object target, String fieldName, Class<T> resultClass) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(target);
		} catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getStaticField(Class<?> clazz, String fieldName, Class<T> resultClass) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(null);
		} catch (Exception e) {
			return null;
		}
	}
	
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
}
