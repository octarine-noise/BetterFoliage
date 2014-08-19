package mods.betterfoliage.common.config;

import java.io.File;
import java.util.List;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.common.collect.Lists;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Config {

	public enum Category {
		extraLeaves, shortGrass, cactus, lilypad, reed, algae, coral, fallingLeaves, connectedGrass;
	}
	
	public static Configuration rawConfig;
	
	public static boolean leavesEnabled;
	public static boolean leavesSkew;
	public static double leavesHOffset;
	public static double leavesVOffset;
	public static double leavesSize;
	
	public static boolean grassEnabled;
	public static boolean grassUseGenerated;
	public static double grassHOffset;
	public static double grassHeightMin;
	public static double grassHeightMax;
	public static double grassSize;
	
	public static boolean cactusEnabled;
	
	public static boolean lilypadEnabled;
	public static double lilypadHOffset;
	public static int lilypadChance;
	
	public static boolean reedEnabled;
	public static double reedHOffset;
	public static double reedHeightMin;
	public static double reedHeightMax;
	public static int reedChance;
	
	public static boolean algaeEnabled;
	public static double algaeHOffset;
	public static double algaeSize;
	public static double algaeHeightMin;
	public static double algaeHeightMax;
	public static int algaePopulation;
	
	public static boolean coralEnabled;
	public static int coralPopulation;
	public static int coralChance;
	public static double coralVOffset;
	public static double coralHOffset;
	public static double coralCrustSize;
	public static double coralSize;
	
	public static boolean leafFXEnabled;
	public static double leafFXSpeed;
	public static double leafFXWindStrength;
	public static double leafFXStormStrength;
	public static double leafFXSize;
	public static double leafFXChance;
	public static double leafFXPerturb;
	public static double leafFXLifetime;
	
	public static boolean ctxGrassClassicEnabled;
	public static boolean ctxGrassAggressiveEnabled;
	
	public static void readConfig(File configFile) {
		rawConfig = new Configuration(configFile, true);
		updateValues();
		if (rawConfig.hasChanged()) rawConfig.save();
	}
	
	public static void updateValues() {
        leavesEnabled = getBoolean(Category.extraLeaves, "enabled", true, "betterfoliage.enabled");
        leavesSkew = getBoolean(Category.extraLeaves, "skewMode", true, "betterfoliage.leavesMode");
        leavesHOffset = getDouble(Category.extraLeaves, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        leavesVOffset = getDouble(Category.extraLeaves, "vOffset", 0.1, 0.0, 0.4, "betterfoliage.vOffset");
        leavesSize = getDouble(Category.extraLeaves, "size", 1.4, 0.75, 1.8, "betterfoliage.size");

        grassEnabled = getBoolean(Category.shortGrass, "enabled", true, "betterfoliage.enabled");
        grassHOffset = getDouble(Category.shortGrass, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        grassHeightMin = getDouble(Category.shortGrass, "heightMin", 0.8, 0.1, 1.5, "betterfoliage.minHeight");
        grassHeightMax = getDouble(Category.shortGrass, "heightMax", 0.8, 0.1, 1.5, "betterfoliage.maxHeight");
        grassSize = getDouble(Category.shortGrass, "size", 1.0, 0.5, 1.5, "betterfoliage.size");
        grassUseGenerated = getBoolean(Category.shortGrass, "useGenerated", false, "betterfoliage.shortGrass.useGenerated");

        cactusEnabled = getBoolean(Category.cactus, "enabled", true, "betterfoliage.enabled");

        lilypadEnabled = getBoolean(Category.lilypad, "enabled", true, "betterfoliage.enabled");
        lilypadHOffset = getDouble(Category.lilypad, "hOffset", 0.1, 0.0, 0.25, "betterfoliage.hOffset");
        lilypadChance = getInt(Category.lilypad, "flowerChance", 16, 0, 64, "betterfoliage.lilypad.flowerChance");

        reedEnabled = getBoolean(Category.reed, "enabled", true, "betterfoliage.enabled");
        reedHOffset = getDouble(Category.reed, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        reedHeightMin = getDouble(Category.reed, "heightMin", 2.0, 1.5, 3.5, "betterfoliage.minHeight");
        reedHeightMax = getDouble(Category.reed, "heightMax", 2.5, 1.5, 3.5, "betterfoliage.maxHeight");
        reedChance = getInt(Category.reed, "chance", 32, 0, 64, "betterfoliage.chance");

        algaeEnabled = getBoolean(Category.algae, "enabled", true, "betterfoliage.enabled");
        algaeHOffset = getDouble(Category.algae, "hOffset", 0.1, 0.0, 0.25, "betterfoliage.hOffset");
        algaeSize = getDouble(Category.algae, "size", 1.0, 0.5, 1.5, "betterfoliage.size");
        algaeHeightMin = getDouble(Category.algae, "heightMin", 0.5, 0.1, 1.5, "betterfoliage.minHeight");
        algaeHeightMax = getDouble(Category.algae, "heightMax", 1.0, 0.1, 1.5, "betterfoliage.maxHeight");
        algaePopulation = getInt(Category.algae, "population", 48, 0, 64, "betterfoliage.population");

        coralEnabled = getBoolean(Category.coral, "enabled", true, "betterfoliage.enabled");
        coralHOffset = getDouble(Category.coral, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        coralVOffset = getDouble(Category.coral, "vOffset", 0.1, 0.0, 0.4, "betterfoliage.vOffset");
        coralSize = getDouble(Category.coral, "size", 1.0, 0.5, 1.5, "betterfoliage.coral.size");
        coralCrustSize = getDouble(Category.coral, "crustSize", 1.0, 0.5, 1.5, "betterfoliage.coral.crustSize");
        coralChance = getInt(Category.coral, "chance", 48, 0, 64, "betterfoliage.coral.chance");
        coralPopulation = getInt(Category.coral, "population", 48, 0, 64, "betterfoliage.population");

        leafFXEnabled = getBoolean(Category.fallingLeaves, "enabled", true, "betterfoliage.enabled");
        leafFXSpeed = getDouble(Category.fallingLeaves, "speed", 0.05, 0.01, 0.15, "betterfoliage.fallingLeaves.speed");
        leafFXWindStrength = getDouble(Category.fallingLeaves, "windStrength", 0.5, 0.1, 2.0, "betterfoliage.fallingLeaves.windStrength");
        leafFXStormStrength = getDouble(Category.fallingLeaves, "stormStrength", 0.8, 0.1, 2.0, "betterfoliage.fallingLeaves.stormStrength");
        leafFXSize = getDouble(Category.fallingLeaves, "size", 0.75, 0.25, 1.5, "betterfoliage.fallingLeaves.size");
        leafFXChance = getDouble(Category.fallingLeaves, "chance", 0.05, 0.001, 1.0, "betterfoliage.fallingLeaves.chance");
        leafFXPerturb = getDouble(Category.fallingLeaves, "perturb", 0.25, 0.01, 1.0, "betterfoliage.fallingLeaves.perturb");
        leafFXLifetime = getDouble(Category.fallingLeaves, "lifetime", 5.0, 1.0, 10.0, "betterfoliage.fallingLeaves.lifetime");
        
        ctxGrassClassicEnabled = getBoolean(Category.connectedGrass, "classic", true, "betterfoliage.connectedGrass.classic");
        ctxGrassAggressiveEnabled= getBoolean(Category.connectedGrass, "aggressive", true, "betterfoliage.connectedGrass.aggressive");
		
		for (Category category : Category.values()) rawConfig.setCategoryLanguageKey(category.toString(), String.format("betterfoliage.%s", category.toString()));
		setOrder(Category.extraLeaves, "enabled", "skewMode", "hOffset", "vOffset", "size");
		setOrder(Category.shortGrass, "enabled", "useGenerated", "hOffset", "heightMin", "heightMax", "size");
	}
	
	@SuppressWarnings("rawtypes")
	public static List<IConfigElement> getConfigRootCategories() {
		List<IConfigElement> result = Lists.newLinkedList();
		for (Category category : Category.values()) {
			ConfigElement<?> element = new ConfigElement(rawConfig.getCategory(category.toString()));
			result.add(element);
		}
		return result;
	}
	
	protected static double getDouble(Category category, String key, double defaultValue, double min, double max, String langKey) {
		Property prop = rawConfig.get(category.toString(), key, defaultValue);
		prop.setMinValue(min);
		prop.setMaxValue(max);
		prop.setLanguageKey(langKey);
		return prop.getDouble();
	}
	
	protected static int getInt(Category category, String key, int defaultValue, int min, int max, String langKey) {
		Property prop = rawConfig.get(category.toString(), key, defaultValue);
		prop.setMinValue(min);
		prop.setMaxValue(max);
		prop.setLanguageKey(langKey);
		return prop.getInt();
	}
	
	protected static boolean getBoolean(Category category, String key, boolean defaultValue, String langKey) {
		Property prop = rawConfig.get(category.toString(), key, defaultValue);
		prop.setLanguageKey(langKey);
		return prop.getBoolean();
	}
	
	protected static void setOrder(Category category, String... properties) {
		rawConfig.setCategoryPropertyOrder(category.toString(), Lists.newArrayList(properties));
	}
	
	@SubscribeEvent
	public void handleConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
	    if (event.modID.equals(BetterFoliage.MOD_ID)) {
	        rawConfig.save();
	        updateValues();
	        Minecraft.getMinecraft().renderGlobal.loadRenderers();
	    }
	}
}
