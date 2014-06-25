package mods.betterfoliage;

import java.io.File;
import java.util.Map;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.config.Config;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;

@Mod(name=BetterFoliage.MOD_NAME, modid=BetterFoliage.MOD_ID, acceptedMinecraftVersions="[1.7.2]", guiFactory="mods.betterfoliage.client.gui.ConfigGuiFactory")
public class BetterFoliage {

	public static final String MOD_ID = "BetterFoliage";
	public static final String MOD_NAME = "Better Foliage";
	
	@Mod.Instance
	public static BetterFoliage instance;
	
	public static Logger log;
	
	public static File configDir;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)  {
		log = event.getModLog();
		if (event.getSide() == Side.CLIENT) {
			configDir = new File(event.getModConfigurationDirectory(), "betterfoliage");
			configDir.mkdir();
			Config.load();
			BetterFoliageClient.preInit();
		}
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (event.getSide() == Side.CLIENT) {
		}
	}
	
	@NetworkCheckHandler
	public boolean checkVersion(Map<String, String> mods, Side side) {
		return true;
	}
}
