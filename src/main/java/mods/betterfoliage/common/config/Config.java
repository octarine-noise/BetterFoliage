package mods.betterfoliage.common.config;

import java.io.File;

import mods.betterfoliage.BetterFoliage;
import net.minecraftforge.common.config.Configuration;

public class Config {

	public static boolean leavesEnabled = true;
	public static boolean leavesSkew = false;
	public static boolean grassEnabled = true;
	public static boolean cactusEnabled = true;
	public static boolean lilypadEnabled = true;
	public static boolean reedEnabled = true;
	
	public static OptionDouble leavesHOffset = new OptionDouble(0.0, 0.4, 0.025, 0.2);
	public static OptionDouble leavesVOffset = new OptionDouble(0.0, 0.4, 0.025, 0.1);
	public static OptionDouble leavesSize = new OptionDouble(0.75, 1.8, 0.05, 1.4);
			
	public static OptionDouble grassHOffset = new OptionDouble(0.0, 0.4, 0.025, 0.2);
	public static OptionDouble grassHeightMin = new OptionDouble(0.1, 1.5, 0.05, 0.5);
	public static OptionDouble grassHeightMax = new OptionDouble(0.1, 1.5, 0.05, 1.0);
	public static OptionDouble grassSize = new OptionDouble(0.5, 1.5, 0.05, 1.0);
	
	public static OptionDouble lilypadHOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	public static OptionInteger lilypadChance = new OptionInteger(0, 64, 1, 16);
	
	public static OptionDouble reedHOffset = new OptionDouble(0.0, 0.25, 0.025, 0.1);
	public static OptionDouble reedHeightMin = new OptionDouble(1.5, 3.5, 0.1, 2.0);
	public static OptionDouble reedHeightMax = new OptionDouble(1.5, 3.5, 0.1, 2.5);
	public static OptionInteger reedChance = new OptionInteger(0, 64, 1, 32);
	
	private Config() {}
	
	public static void load() {
		Configuration config = new Configuration(new File(BetterFoliage.configDir, "betterfoliage.cfg"));
		config.load();
		
		leavesEnabled = config.get("render", "leavesEnabled", true).getBoolean(true);
		leavesSkew = config.get("render", "leavesOffsetSkew", false).getBoolean(false);
		loadValue(config, "render", "leavesHorizontalOffset", leavesHOffset);
		loadValue(config, "render", "leavesVerticalOffset", leavesVOffset);
		loadValue(config, "render", "leavesSize", leavesSize);
		
		grassEnabled = config.get("render", "grassEnabled", true).getBoolean(true);
		loadValue(config, "render", "grassHorizontalOffset", grassHOffset);
		loadValue(config, "render", "grassHeightMin", grassHeightMin);
		loadValue(config, "render", "grassHeightMax", grassHeightMax);
		if (grassHeightMin.value > grassHeightMax.value) grassHeightMin.value = grassHeightMax.value;
		
		grassEnabled = config.get("render", "cactusEnabled", true).getBoolean(true);
		grassEnabled = config.get("render", "lilypadEnabled", true).getBoolean(true);
		loadValue(config, "render", "lilypadHorizontalOffset", lilypadHOffset);
		loadValue(config, "render", "lilypadChance", lilypadChance);
		
		reedEnabled = config.get("render", "reedEnabled", true).getBoolean(true);
		loadValue(config, "render", "reedHeightMin", reedHeightMin);
		loadValue(config, "render", "reedHeightMax", reedHeightMax);
		loadValue(config, "render", "reedChance", reedChance);
		if (reedHeightMin.value > reedHeightMax.value) reedHeightMin.value = reedHeightMax.value;
		
		if (config.hasChanged()) config.save();
	}
	
	public static void save() {
		Configuration config = new Configuration(new File(BetterFoliage.configDir, "betterfoliage.cfg"));
		config.load();
		
		config.get("render", "leavesEnabled", true).set(leavesEnabled);
		config.get("render", "leavesOffsetSkew", false).set(leavesSkew);
		saveValue(config, "render", "leavesHorizontalOffset", leavesHOffset);
		saveValue(config, "render", "leavesVerticalOffset", leavesVOffset);
		saveValue(config, "render", "leavesSize", leavesSize);
		
		config.get("render", "grassEnabled", true).set(grassEnabled);
		saveValue(config, "render", "grassHorizontalOffset", grassHOffset);
		saveValue(config, "render", "grassHeightMin", grassHeightMin);
		saveValue(config, "render", "grassHeightMax", grassHeightMax);
		
		config.get("render", "cactusEnabled", true).set(cactusEnabled);
		config.get("render", "lilypadEnabled", true).set(lilypadEnabled);
		saveValue(config, "render", "lilypadHorizontalOffset", lilypadHOffset);
		saveValue(config, "render", "lilypadChance", lilypadChance);
		
		config.get("render", "reedEnabled", true).set(reedEnabled);
		saveValue(config, "render", "reedHeightMin", reedHeightMin);
		saveValue(config, "render", "reedHeightMax", reedHeightMax);
		saveValue(config, "render", "reedChance", reedChance);
		
		if (config.hasChanged()) config.save();
	}
	
	protected static void saveValue(Configuration config, String category, String key, OptionDouble option) {
		config.get(category, key, option.value).set(option.value);
	}
	
	protected static void loadValue(Configuration config, String category, String key, OptionDouble option) {
		option.value = config.get(category, key, option.value).getDouble(option.value);
		if (option.value > option.max) {
			option.value = option.max;
			saveValue(config, category, key, option);
		}
		if (option.value < option.min) {
			option.value = option.min;
			saveValue(config, category, key, option);
		}
	}
	
	protected static void saveValue(Configuration config, String category, String key, OptionInteger option) {
		config.get(category, key, option.value).set(option.value);
	}
	
	protected static void loadValue(Configuration config, String category, String key, OptionInteger option) {
		option.value = config.get(category, key, option.value).getInt(option.value);
		if (option.value > option.max) {
			option.value = option.max;
			saveValue(config, category, key, option);
		}
		if (option.value < option.min) {
			option.value = option.min;
			saveValue(config, category, key, option);
		}
	}
}
