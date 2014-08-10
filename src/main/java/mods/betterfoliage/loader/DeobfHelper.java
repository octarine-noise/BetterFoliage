package mods.betterfoliage.loader;

import java.util.Map;

import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class DeobfHelper {

	private static Map<String, String> obfClasses = Maps.newHashMap();
	private static Map<String, String> obfElements = Maps.newHashMap();
	private static Map<String, String> srgElements = Maps.newHashMap();
	
	public static void init() {
		String mcVersion = FMLInjectionData.data()[4].toString();
		srgElements.put("domainResourceManagers", "field_110548_a");
		srgElements.put("mapRegisteredSprites", "field_110574_e");
		if ("1.7.2".equals(mcVersion)) {
			obfClasses.put("net/minecraft/client/renderer/RenderBlocks", "ble");
			obfClasses.put("net/minecraft/world/IBlockAccess", "afx");
			obfClasses.put("net/minecraft/block/Block", "ahu");
			obfClasses.put("net/minecraft/client/multiplayer/WorldClient", "biz");
			obfClasses.put("net/minecraft/world/World", "afn");
			
			obfElements.put("blockAccess", "a");
			obfElements.put("renderBlockByRenderType", "b");
			obfElements.put("mapRegisteredSprites", "bpr");
			obfElements.put("doVoidFogParticles", "C");
		} else if ("1.7.10".equals(mcVersion)) {
			obfClasses.put("net/minecraft/client/renderer/RenderBlocks", "blm");
			obfClasses.put("net/minecraft/world/IBlockAccess", "ahl");
			obfClasses.put("net/minecraft/block/Block", "aji");
			obfClasses.put("net/minecraft/client/multiplayer/WorldClient", "bjf");
			obfClasses.put("net/minecraft/world/World", "ahb");
			
			obfElements.put("blockAccess", "a");
			obfElements.put("renderBlockByRenderType", "b");
			obfElements.put("mapRegisteredSprites", "bpr");
			obfElements.put("doVoidFogParticles", "C");
		}
	}
	
	public static String transformClassName(String className) {
		return obfClasses.containsKey(className) ? obfClasses.get(className) : className;
	}
	
	public static String transformElementName(String elementName) {
		return obfElements.containsKey(elementName) ? obfElements.get(elementName) : elementName;
	}
	
	public static String transformElementSearge(String elementName) {
		return srgElements.containsKey(elementName) ? srgElements.get(elementName) : elementName;
	}
	
	public static String transformSignature(String signature) {
		String result = signature;
		boolean hasChanged = false;
		do {
			hasChanged = false;
			for (Map.Entry<String, String> entry : obfClasses.entrySet()) if (result.contains("L" + entry.getKey() + ";")) {
				result = result.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
				hasChanged = true;
			}
		} while(hasChanged);
		return result;
	}
}
