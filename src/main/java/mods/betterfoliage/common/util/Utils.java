package mods.betterfoliage.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

import mods.betterfoliage.loader.DeobfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Charsets;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class Utils {

	/** Hide constructor */
	private Utils() {}
	
	/** 
	 * @return (({@link SimpleReloadableResourceManager}) Minecraft.getMinecraft().getResourceManager()).domainResourceManagers
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, IResourceManager> getDomainResourceManagers() {
		IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
		Map<String, IResourceManager> result = getField(manager, DeobfHelper.transformElementSearge("domainResourceManagers"), Map.class);
		if (result == null) result = getField(manager, "domainResourceManagers", Map.class);
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
	
	/** Check for the existence of a {@link IResource}
	 * @param resourceLocation
	 * @return true if the resource exists
	 */
	public static boolean resourceExists(ResourceLocation resourceLocation) {
		try {
			IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			if (resource != null) return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	/** Copy a text file from a resource to the filesystem 
	 * @param resourceLocation resource location of text file
	 * @param target target file
	 * @throws IOException 
	 */
	public static void copyFromTextResource(ResourceLocation resourceLocation, File target) throws IOException {
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
	}
}
