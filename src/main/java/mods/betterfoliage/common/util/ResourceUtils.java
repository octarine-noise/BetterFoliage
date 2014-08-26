package mods.betterfoliage.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.loader.DeobfHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;

public class ResourceUtils {

	/** Hide constructor */
	private ResourceUtils() {}
	
	/** 
	 * @return (({@link SimpleReloadableResourceManager}) Minecraft.getMinecraft().getResourceManager()).domainResourceManagers
	 */
	public static Map<String, IResourceManager> getDomainResourceManagers() {
        try {
            return ReflectionHelper.<Map<String, IResourceManager>, SimpleReloadableResourceManager> getPrivateValue(
                SimpleReloadableResourceManager.class, (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager(), DeobfHelper.transformElementSearge("domainResourceManagers"), "domainResourceManagers"
            );
        } catch (UnableToAccessFieldException e) {
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
	
	public static Iterable<String> getLines(ResourceLocation resource) {
	    BufferedReader reader = null;
	    List<String> result = Lists.newArrayList();
        try {
            reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(resource).getInputStream(), Charsets.UTF_8));
            String line = reader.readLine();
            while(line != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) result.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            BetterFoliage.log.warn(String.format("Error reading resource %s", resource.toString()));
            return Lists.newArrayList();
        }
        return result;
	}
}
