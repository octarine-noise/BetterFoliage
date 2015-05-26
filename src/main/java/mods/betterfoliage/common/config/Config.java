package mods.betterfoliage.common.config;

import java.io.File;
import java.util.List;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.gui.AlternateTextBooleanEntry;
import mods.betterfoliage.client.gui.BiomeListConfigEntry;
import mods.betterfoliage.client.gui.NonVerboseArrayEntry;
import mods.betterfoliage.client.misc.BlockMatcher;
import mods.betterfoliage.client.util.BiomeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class Config {

	public enum Category {
	    blockTypes, extraLeaves, shortGrass, cactus, lilypad, reed, algae, coral, netherrack, fallingLeaves, risingSoul, connectedGrass, roundLogs;
	}
	
	/** {@link Configuration} object bound to the config file */
	public static Configuration rawConfig;
	
	// block matchers
    public static BlockMatcher leaves = new BlockMatcher();
    public static BlockMatcher crops = new BlockMatcher();
    public static BlockMatcher dirt = new BlockMatcher();
    public static BlockMatcher grass = new BlockMatcher();
    public static BlockMatcher logs = new BlockMatcher();
    
	// extracted config values
	public static boolean leavesEnabled;
	public static boolean leavesSkew;
	public static boolean leavesDense;
	public static double leavesHOffset;
	public static double leavesVOffset;
	public static double leavesSize;
	
	public static boolean grassEnabled;
	public static boolean grassUseGenerated;
	public static double grassHOffset;
	public static double grassHeightMin;
	public static double grassHeightMax;
	public static double grassSize;
	public static boolean grassShaderWind;
	public static boolean myceliumEnabled;
	public static boolean grassSnowEnabled;
	public static double grassSaturationThreshold;
	
	public static boolean cactusEnabled;
	
	public static boolean lilypadEnabled;
	public static double lilypadHOffset;
	public static int lilypadChance;
	
	public static boolean reedEnabled;
	public static double reedHOffset;
	public static double reedHeightMin;
	public static double reedHeightMax;
	public static int reedPopulation;
	public static boolean reedShaderWind;
	
	public static boolean algaeEnabled;
	public static double algaeHOffset;
	public static double algaeSize;
	public static double algaeHeightMin;
	public static double algaeHeightMax;
	public static int algaePopulation;
	
	public static boolean coralEnabled;
	public static boolean coralShallowWater;
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
	
	public static boolean soulFXEnabled;
	public static double soulFXChance;
	public static double soulFXSpeed;
	public static double soulFXPerturb;
	public static double soulFXHeadSize;
	public static double soulFXTrailSize;
	public static double soulFXOpacity;
	public static double soulFXSizeDecay;
	public static double soulFXOpacityDecay;
	public static double soulFXLifetime;
	public static int soulFXTrailLength;
	public static int soulFXTrailDensity;
	
	public static boolean netherrackEnabled;
    public static double netherrackHOffset;
    public static double netherrackHeightMin;
    public static double netherrackHeightMax;
    public static double netherrackSize;
	    
	public static boolean ctxGrassClassicEnabled;
	public static boolean ctxGrassAggressiveEnabled;
	
	public static boolean logsEnabled;
	public static double logsSmallRadius;
	public static double logsLargeRadius;
	public static boolean logsConnect;
	public static boolean logsConnectGrass;
	
	public static List<Integer> reedBiomeList = Lists.newArrayList();
	public static List<Integer> algaeBiomeList = Lists.newArrayList();
	public static List<Integer> coralBiomeList = Lists.newArrayList();
	public static Predicate<BiomeGenBase> reedBiomeDefaults = BiomeUtils.biomeTempRainFilter(0.4f, null, 0.4f, null);
	public static Predicate<BiomeGenBase> algaeBiomeDefaults = BiomeUtils.biomeClassNameFilter("river", "ocean"); 
	public static Predicate<BiomeGenBase> coralBiomeDefaults = BiomeUtils.biomeClassNameFilter("river", "ocean", "beach");
	
	/** Read the config file
	 * @param configFile
	 */
	public static void readConfig(File configFile) {
		rawConfig = new Configuration(configFile, true);
		updateValues();
		if (rawConfig.hasChanged()) rawConfig.save();
	}
	
	/** Extract the config properties to static fields for quick access */
	public static void updateValues() {
        leavesEnabled = getBoolean(Category.extraLeaves, "enabled", true, "betterfoliage.enabled");
        leavesSkew = getBoolean(Category.extraLeaves, "skewMode", false, "betterfoliage.leavesMode");
        leavesHOffset = getDouble(Category.extraLeaves, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        leavesVOffset = getDouble(Category.extraLeaves, "vOffset", 0.1, 0.0, 0.4, "betterfoliage.vOffset");
        leavesSize = getDouble(Category.extraLeaves, "size", 1.4, 0.75, 2.5, "betterfoliage.size");
        leavesDense = getBoolean(Category.extraLeaves, "dense", false, "betterfoliage.leaves.dense");
        
        grassEnabled = getBoolean(Category.shortGrass, "enabled", true, "betterfoliage.enabled");
        grassHOffset = getDouble(Category.shortGrass, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        grassHeightMin = getDouble(Category.shortGrass, "heightMin", 0.6, 0.1, 2.5, "betterfoliage.minHeight");
        grassHeightMax = getDouble(Category.shortGrass, "heightMax", 0.8, 0.1, 2.5, "betterfoliage.maxHeight");
        grassSize = getDouble(Category.shortGrass, "size", 1.0, 0.5, 1.5, "betterfoliage.size");
        grassUseGenerated = getBoolean(Category.shortGrass, "useGenerated", false, "betterfoliage.shortGrass.useGenerated");
        grassHeightMin = clampDoubleToMax(Category.shortGrass, "heightMin", "heightMax");
        grassShaderWind = getBoolean(Category.shortGrass, "shaderWind", true, "betterfoliage.shaderWind");
        myceliumEnabled = getBoolean(Category.shortGrass, "myceliumEnabled", true, "betterfoliage.shortGrass.myceliumEnabled");
        grassSnowEnabled = getBoolean(Category.shortGrass, "snowEnabled", true, "betterfoliage.shortGrass.grassSnowEnabled");
        grassSaturationThreshold = getDouble(Category.shortGrass, "saturationThreshold", 0.1, 0.0, 1.0, "betterfoliage.shortGrass.grassSaturationThreshold");
        
        cactusEnabled = getBoolean(Category.cactus, "enabled", true, "betterfoliage.enabled");

        lilypadEnabled = getBoolean(Category.lilypad, "enabled", true, "betterfoliage.enabled");
        lilypadHOffset = getDouble(Category.lilypad, "hOffset", 0.1, 0.0, 0.25, "betterfoliage.hOffset");
        lilypadChance = getInt(Category.lilypad, "flowerChance", 16, 0, 64, "betterfoliage.lilypad.flowerChance");

        reedEnabled = getBoolean(Category.reed, "enabled", true, "betterfoliage.enabled");
        reedHOffset = getDouble(Category.reed, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        reedHeightMin = getDouble(Category.reed, "heightMin", 2.0, 1.5, 3.5, "betterfoliage.minHeight");
        reedHeightMax = getDouble(Category.reed, "heightMax", 2.5, 1.5, 3.5, "betterfoliage.maxHeight");
        reedPopulation = getInt(Category.reed, "population", 32, 0, 64, "betterfoliage.population");
        reedHeightMin = clampDoubleToMax(Category.reed, "heightMin", "heightMax");
        reedBiomeList = getIntList(Category.reed, "reedBiomeList", reedBiomeList, "betterfoliage.reed.biomeList");
        reedShaderWind = getBoolean(Category.reed, "shaderWind", true, "betterfoliage.shaderWind");
        
        algaeEnabled = getBoolean(Category.algae, "enabled", true, "betterfoliage.enabled");
        algaeHOffset = getDouble(Category.algae, "hOffset", 0.1, 0.0, 0.25, "betterfoliage.hOffset");
        algaeSize = getDouble(Category.algae, "size", 1.0, 0.5, 1.5, "betterfoliage.size");
        algaeHeightMin = getDouble(Category.algae, "heightMin", 0.5, 0.1, 1.5, "betterfoliage.minHeight");
        algaeHeightMax = getDouble(Category.algae, "heightMax", 1.0, 0.1, 1.5, "betterfoliage.maxHeight");
        algaePopulation = getInt(Category.algae, "population", 48, 0, 64, "betterfoliage.population");
        algaeHeightMin = clampDoubleToMax(Category.algae, "heightMin", "heightMax");
        algaeBiomeList = getIntList(Category.algae, "algaeBiomeList", algaeBiomeList, "betterfoliage.algae.biomeList");
        
        coralEnabled = getBoolean(Category.coral, "enabled", true, "betterfoliage.enabled");
        coralShallowWater = getBoolean(Category.coral, "shallowWater", false, "betterfoliage.coral.shallowWater");
        coralHOffset = getDouble(Category.coral, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        coralVOffset = getDouble(Category.coral, "vOffset", 0.1, 0.0, 0.4, "betterfoliage.vOffset");
        coralSize = getDouble(Category.coral, "size", 0.7, 0.5, 1.5, "betterfoliage.coral.size");
        coralCrustSize = getDouble(Category.coral, "crustSize", 1.4, 0.5, 1.5, "betterfoliage.coral.crustSize");
        coralChance = getInt(Category.coral, "chance", 32, 0, 64, "betterfoliage.coral.chance");
        coralPopulation = getInt(Category.coral, "population", 48, 0, 64, "betterfoliage.population");
        coralBiomeList = getIntList(Category.coral, "coralBiomeList", coralBiomeList, "betterfoliage.coral.biomeList");
        
        leafFXEnabled = getBoolean(Category.fallingLeaves, "enabled", true, "betterfoliage.enabled");
        leafFXSpeed = getDouble(Category.fallingLeaves, "speed", 0.05, 0.01, 0.15, "betterfoliage.fallingLeaves.speed");
        leafFXWindStrength = getDouble(Category.fallingLeaves, "windStrength", 0.5, 0.1, 2.0, "betterfoliage.fallingLeaves.windStrength");
        leafFXStormStrength = getDouble(Category.fallingLeaves, "stormStrength", 0.8, 0.1, 2.0, "betterfoliage.fallingLeaves.stormStrength");
        leafFXSize = getDouble(Category.fallingLeaves, "size", 0.75, 0.25, 1.5, "betterfoliage.fallingLeaves.size");
        leafFXChance = getDouble(Category.fallingLeaves, "chance", 0.05, 0.001, 1.0, "betterfoliage.fallingLeaves.chance");
        leafFXPerturb = getDouble(Category.fallingLeaves, "perturb", 0.25, 0.01, 1.0, "betterfoliage.fallingLeaves.perturb");
        leafFXLifetime = getDouble(Category.fallingLeaves, "lifetime", 5.0, 1.0, 15.0, "betterfoliage.fallingLeaves.lifetime");
        deleteKey(Category.fallingLeaves, "opacityHack");
        
        soulFXEnabled = getBoolean(Category.risingSoul, "enabled", true, "betterfoliage.enabled");
        soulFXChance = getDouble(Category.risingSoul, "chance", 0.02, 0.001, 1.0, "betterfoliage.risingSoul.chance");
        soulFXSpeed = getDouble(Category.risingSoul, "speed", 0.1, 0.025, 0.25, "betterfoliage.risingSoul.speed");
        soulFXPerturb = getDouble(Category.risingSoul, "perturb", 0.05, 0.01, 0.25, "betterfoliage.risingSoul.perturb");
        soulFXHeadSize = getDouble(Category.risingSoul, "headSize", 1.0, 0.25, 1.5, "betterfoliage.risingSoul.headSize");
        soulFXTrailSize = getDouble(Category.risingSoul, "trailSize", 0.75, 0.25, 1.5, "betterfoliage.risingSoul.trailSize");
        soulFXOpacity = getDouble(Category.risingSoul, "opacity", 0.5, 0.05, 1.0, "betterfoliage.risingSoul.opacity");
        soulFXSizeDecay = getDouble(Category.risingSoul, "sizeDecay", 0.97, 0.5, 1.0, "betterfoliage.risingSoul.sizeDecay");
        soulFXOpacityDecay = getDouble(Category.risingSoul, "opacityDecay", 0.97, 0.5, 1.0, "betterfoliage.risingSoul.opacityDecay");
        soulFXLifetime = getDouble(Category.risingSoul, "lifetime", 4.0, 1.0, 15.0, "betterfoliage.risingSoul.lifetime");
        soulFXTrailLength = getInt(Category.risingSoul, "trailLength", 48, 2, 128, "betterfoliage.risingSoul.trailLength");
        soulFXTrailDensity = getInt(Category.risingSoul, "trailDensity", 3, 1, 16, "betterfoliage.risingSoul.trailDensity");
        
        netherrackEnabled = getBoolean(Category.netherrack, "enabled", true, "betterfoliage.enabled");
        netherrackHOffset = getDouble(Category.netherrack, "hOffset", 0.2, 0.0, 0.4, "betterfoliage.hOffset");
        netherrackHeightMin = getDouble(Category.netherrack, "heightMin", 0.6, 0.1, 1.5, "betterfoliage.minHeight");
        netherrackHeightMax = getDouble(Category.netherrack, "heightMax", 0.8, 0.1, 1.5, "betterfoliage.maxHeight");
        netherrackSize = getDouble(Category.netherrack, "size", 1.0, 0.5, 1.5, "betterfoliage.size");
        netherrackHeightMin = clampDoubleToMax(Category.netherrack, "heightMin", "heightMax");
        
        ctxGrassClassicEnabled = getBoolean(Category.connectedGrass, "classic", true, "betterfoliage.connectedGrass.classic");
        ctxGrassAggressiveEnabled= getBoolean(Category.connectedGrass, "aggressive", true, "betterfoliage.connectedGrass.aggressive");
		
        logsEnabled = getBoolean(Category.roundLogs, "enabled", true, "betterfoliage.enabled");
        logsSmallRadius = getDouble(Category.roundLogs, "smallRadius", 0.25, 0.0, 0.5, "betterfoliage.roundLogs.smallRadius");
        logsLargeRadius = getDouble(Category.roundLogs, "largeRadius", 0.45, 0.0, 0.5, "betterfoliage.roundLogs.largeRadius");
        logsConnect = getBoolean(Category.roundLogs, "connect", true, "betterfoliage.roundLogs.connect");
        logsConnectGrass = getBoolean(Category.roundLogs, "connectGrass", true, "betterfoliage.roundLogs.connectGrass");
        
        updateBlockMatcher(dirt, Category.blockTypes, "dirtWhitelist", "betterfoliage.blockTypes.dirtWhitelist", "dirtBlacklist", "betterfoliage.blockTypes.dirtBlacklist", new ResourceLocation("betterfoliage:classesDirtDefault.cfg"));
        updateBlockMatcher(grass, Category.blockTypes, "grassWhitelist", "betterfoliage.blockTypes.grassWhitelist", "grassBlacklist", "betterfoliage.blockTypes.grassBlacklist", new ResourceLocation("betterfoliage:classesGrassDefault.cfg"));
        updateBlockMatcher(leaves, Category.blockTypes, "leavesWhitelist", "betterfoliage.blockTypes.leavesWhitelist", "leavesBlacklist", "betterfoliage.blockTypes.leavesBlacklist", new ResourceLocation("betterfoliage:classesLeavesDefault.cfg"));
        updateBlockMatcher(crops, Category.blockTypes, "cropWhitelist", "betterfoliage.blockTypes.cropWhitelist", "cropBlacklist", "betterfoliage.blockTypes.cropBlacklist", new ResourceLocation("betterfoliage:classesCropDefault.cfg"));
        updateBlockMatcher(logs, Category.blockTypes, "logWhitelist", "betterfoliage.blockTypes.logWhitelist", "logBlacklist", "betterfoliage.blockTypes.logBlacklist", new ResourceLocation("betterfoliage:classesLogDefault.cfg"));
        
        rawConfig.getCategory(Category.extraLeaves.toString()).get("skewMode").setConfigEntryClass(AlternateTextBooleanEntry.class);
        rawConfig.getCategory(Category.extraLeaves.toString()).get("dense").setConfigEntryClass(AlternateTextBooleanEntry.class);
        rawConfig.getCategory(Category.reed.toString()).get("reedBiomeList").setConfigEntryClass(BiomeListConfigEntry.class);
        rawConfig.getCategory(Category.algae.toString()).get("algaeBiomeList").setConfigEntryClass(BiomeListConfigEntry.class);
        rawConfig.getCategory(Category.coral.toString()).get("coralBiomeList").setConfigEntryClass(BiomeListConfigEntry.class);
        
		for (Category category : Category.values()) rawConfig.setCategoryLanguageKey(category.toString(), String.format("betterfoliage.%s", category.toString()));
		
		setOrder(Category.extraLeaves, "enabled", "dense", "skewMode", "hOffset", "vOffset", "size");
		setOrder(Category.shortGrass, "enabled", "myceliumEnabled", "snowEnabled", "useGenerated", "hOffset", "heightMin", "heightMax", "size", "shaderWind", "saturationThreshold");
		setOrder(Category.lilypad, "enabled", "hOffset", "flowerChance");
		setOrder(Category.reed, "enabled", "hOffset", "heightMin", "heightMax", "population", "biomeList", "shaderWind");
		setOrder(Category.algae, "enabled", "hOffset", "heightMin", "heightMax", "size", "population", "biomeList");
		setOrder(Category.coral, "enabled", "shallowWater", "hOffset", "vOffset", "size", "crustSize", "population", "chance", "biomeList");
		setOrder(Category.netherrack, "enabled", "hOffset", "heightMin", "heightMax", "size");
		setOrder(Category.fallingLeaves, "enabled", "chance", "size", "lifetime", "speed", "windStrength", "stormStrength", "perturb");
		setOrder(Category.risingSoul, "enabled", "chance", "speed", "perturb", "headSize", "trailSize", "sizeDecay", "opacity", "opacityDecay", "lifetime", "trailLength", "trailDensity");
		setOrder(Category.connectedGrass, "classic", "aggressive");
		setOrder(Category.roundLogs, "enabled", "connect", "connectGrass", "smallRadius", "largeRadius");
	}
	
	public static void setupDefaultBiomes() {
	    List<BiomeGenBase> biomes = BiomeUtils.getAllBiomes();
	    reedBiomeList = BiomeUtils.getFilteredBiomeIds(biomes, reedBiomeDefaults);
	    algaeBiomeList = BiomeUtils.getFilteredBiomeIds(biomes, algaeBiomeDefaults);
	    coralBiomeList = BiomeUtils.getFilteredBiomeIds(biomes, coralBiomeDefaults);
	}
	
	public static List<IConfigElement> getConfigRootElements() {
		List<IConfigElement> result = Lists.newLinkedList();
		for (Category category : Category.values()) {
		    if (category == Category.connectedGrass) continue;
			ConfigElement element = new ConfigElement(rawConfig.getCategory(category.toString()));
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
	
    protected static double clampDoubleToMax(Category category, String keySmaller, String keyLarger) {
        ConfigCategory cfgCat = rawConfig.getCategory(category.toString());
        Property smaller = cfgCat.get(keySmaller);
        Property larger = cfgCat.get(keyLarger);
        if (smaller.getDouble() > larger.getDouble()) smaller.set(larger.getDouble());
        return smaller.getDouble();
    }
	   
	protected static int getInt(Category category, String key, int defaultValue, int min, int max, String langKey) {
		Property prop = rawConfig.get(category.toString(), key, defaultValue);
		prop.setMinValue(min);
		prop.setMaxValue(max);
		prop.setLanguageKey(langKey);
		return prop.getInt();
	}
	
	protected static List<Integer> getIntList(Category category, String key, List<Integer> defaultList, String langKey) {
	    int[] defaults = new int[]{};
	    if (defaultList != null) {
	        defaults = new int[defaultList.size()];
	        int idx = 0;
	        for (Integer value : defaultList) defaults[idx++] = value;
	    }
	    
	    Property prop = rawConfig.get(category.toString(), key, defaults);
	    prop.setLanguageKey(langKey);
	    
	    int[] values = prop.getIntList();
	    List<Integer> result = Lists.newArrayListWithCapacity(values.length);
	    for (int value : values) result.add(value);
	    return result;
	}
	    
    protected static boolean getBoolean(Category category, String key, boolean defaultValue, String langKey) {
        Property prop = rawConfig.get(category.toString(), key, defaultValue);
        prop.setLanguageKey(langKey);
        return prop.getBoolean();
    }
    
	protected static void updateBlockMatcher(BlockMatcher bm, Category category, String whitelistKey, String whitelistLangKey, String blacklistKey, String blacklistLangKey, ResourceLocation defaults) {
        List<String> defaultWhitelist = Lists.newLinkedList();
        List<String> defaultBlacklist = Lists.newLinkedList();
        BlockMatcher.loadDefaultLists(defaults, defaultBlacklist, defaultWhitelist);
        
        Property whitelist = rawConfig.get(category.toString(), whitelistKey, defaultWhitelist.toArray(new String[]{}));
        Property blacklist = rawConfig.get(category.toString(), blacklistKey, defaultBlacklist.toArray(new String[]{}));
        
        whitelist.setLanguageKey(whitelistLangKey);
        blacklist.setLanguageKey(blacklistLangKey);
	    whitelist.setConfigEntryClass(NonVerboseArrayEntry.class);
	    blacklist.setConfigEntryClass(NonVerboseArrayEntry.class);
	    
	    bm.updateClassLists(whitelist.getStringList(), blacklist.getStringList());
	}
	
	protected static void setOrder(Category category, String... properties) {
		rawConfig.setCategoryPropertyOrder(category.toString(), Lists.newArrayList(properties));
	}

	protected static void deleteKey(Category category, String key) {
	    rawConfig.getCategory(category.toString()).remove(key);
	}
	
	@SubscribeEvent
	public void handleConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
	    if (event.modID.equals(BetterFoliage.MOD_ID)) {
	        updateValues();
	        boolean blocksUpdated = rawConfig.getCategory(Category.blockTypes.toString()).hasChanged();
	        blocksUpdated |= rawConfig.getCategory(Category.shortGrass.toString()).get("saturationThreshold").hasChanged();
	        if (rawConfig.hasChanged()) rawConfig.save();
	        
	        if (blocksUpdated)
	        	Minecraft.getMinecraft().refreshResources();
	        else
	        	Minecraft.getMinecraft().renderGlobal.loadRenderers();
	    }
	}
}
