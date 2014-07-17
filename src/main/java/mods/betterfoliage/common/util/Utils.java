package mods.betterfoliage.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

import com.google.common.base.Charsets;

import mods.betterfoliage.loader.DeobfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class Utils {

	private Utils() {}
	
	@SuppressWarnings("unchecked")
	public static Map<String, IResourceManager> getDomainResourceManagers() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		Map<String, IResourceManager> result = getField(manager, "domainResourceManagers", Map.class);
		if (result == null) result = getField(manager, DeobfHelper.transformElementSearge("domainResourceManagers"), Map.class);
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
	
	public static boolean resourceExists(ResourceLocation resourceLocation) {
		try {
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			if (resource != null) return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	public static void copyFromTextResource(ResourceLocation resourceLocation, File target) {
		try {
			IResource defaults = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			BufferedReader reader = new BufferedReader(new InputStreamReader(defaults.getInputStream(), Charsets.UTF_8));
			FileWriter writer = new FileWriter(target);
			
			String line = reader.readLine();
			while(line != null) {
				writer.write(line + System.lineSeparator());
				line = reader.readLine();
			}
			reader.close();
			writer.close();
		} catch(IOException e) {
		}
	}
}
