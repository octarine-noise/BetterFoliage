package mods.betterfoliage.loader;

import java.util.Map;

import net.minecraftforge.fml.relauncher.FMLInjectionData;

import com.google.common.collect.Maps;

public class DeobfHelper {

	private static Map<String, String> obfClasses = Maps.newHashMap();
	private static Map<String, String> obfElements = Maps.newHashMap();
	private static Map<String, String> srgElements = Maps.newHashMap();
	
	public static void init() {
		String mcVersion = FMLInjectionData.data()[4].toString();
		srgElements.put("domainResourceManagers", "field_110548_a");
		srgElements.put("variants", "field_177612_i");
		srgElements.put("models", "field_177611_h");
		srgElements.put("textureMap", "field_177609_j");

		if ("1.8".equals(mcVersion)) {
		    obfClasses.put("net/minecraft/block/Block", "atr");
		    obfClasses.put("net/minecraft/world/IBlockAccess", "ard");
		    obfClasses.put("net/minecraft/util/BlockPos", "dt");
		    obfClasses.put("net/minecraft/block/state/IBlockState", "bec");
		    obfClasses.put("net/minecraft/world/World", "aqu");
		    obfClasses.put("net/minecraft/client/multiplayer/WorldClient", "cen");
		    obfClasses.put("net/minecraft/client/renderer/BlockRendererDispatcher", "cll");
		    obfClasses.put("net/minecraft/client/renderer/BlockModelRenderer", "cln");
		    obfClasses.put("net/minecraft/util/EnumWorldBlockLayer", "aql");
		    obfClasses.put("net/minecraft/client/renderer/chunk/RenderChunk", "cop");
		    obfClasses.put("net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator", "coa");
		    obfClasses.put("net/minecraft/client/renderer/WorldRenderer", "civ");
		    obfClasses.put("net/minecraft/client/resources/model/IBakedModel", "cxe");
		    obfClasses.put("net/minecraft/util/IRegistry", "ez");
		    obfClasses.put("net/minecraft/client/resources/model/ModelBakery", "cxh");
		    
		    obfElements.put("mapRegisteredSprites", "j");
		    obfElements.put("doVoidFogParticles", "b");
		    obfElements.put("setupModelRegistry", "a");
		    obfElements.put("loadModelsCheck", "h");
	        obfElements.put("renderBlock", "a");
	        obfElements.put("rebuildChunk", "b");
		}
	}
	
	/** Transform a class name from MCP to obfuscated names.
	 * @param className MCP name
	 * @return obfuscated name
	 */
	public static String transformClassName(String className) {
		return obfClasses.containsKey(className) ? obfClasses.get(className) : className;
	}
	
	/** Transform a method or field name from MCP to obfuscated names.
	 * @param elementName MCP name
	 * @return obfuscated name
	 */
	public static String transformElementName(String elementName) {
		return obfElements.containsKey(elementName) ? obfElements.get(elementName) : elementName;
	}
	
	/** Transform a method or field name from MCP to SRG names.
	 * @param elementName MCP name
	 * @return SRG name
	 */
	public static String transformElementSearge(String elementName) {
		return srgElements.containsKey(elementName) ? srgElements.get(elementName) : elementName;
	}
	
	/** Transform an ASM signature from MCP to obfuscated names.
	 * @param signature MCP signature
	 * @return obfuscated signature
	 */
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
