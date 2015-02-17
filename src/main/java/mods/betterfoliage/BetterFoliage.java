package mods.betterfoliage;

import java.io.File;
import java.util.Map;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Logger;


@Mod(name=BetterFoliage.MOD_NAME, modid=BetterFoliage.MOD_ID, acceptedMinecraftVersions=BetterFoliage.MC_VERSIONS, guiFactory=BetterFoliage.GUI_FACTORY, dependencies=BetterFoliage.DEPS)
public class BetterFoliage {

	public static final String MOD_ID = "BetterFoliage";
	public static final String MOD_NAME = "Better Foliage";
	public static final String MC_VERSIONS = "[1.8]";
	public static final String DEPS = "required-after:Forge@[11.14.0.1292,)";
	public static final String GUI_FACTORY = "mods.betterfoliage.client.gui.ConfigGuiFactory";
	public static final String DOMAIN = "betterfoliage";
	
	@Mod.Instance
	public static BetterFoliage instance;
	
	public static Logger log;
	
	public static File configDir;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)  {
	    log = event.getModLog();
	    if (event.getSide() == Side.CLIENT) {
    		configDir = new File(event.getModConfigurationDirectory(), MOD_ID);
    		configDir.mkdir();
	    } else {
	        
	    }
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)  {
	    if (event.getSide() == Side.CLIENT) {
	        Config.setupDefaultBiomes();
            Config.readConfig(new File(configDir, "betterfoliage.cfg"));
            BetterFoliageClient.postInit();
        }
	}
	
	/** Mod is cosmetic only, always allow connection.
	 * @param mods list of mods and versions of the remote party
	 * @param side side of remote party
	 * @return true
	 */
	@NetworkCheckHandler
	public boolean checkVersion(Map<String, String> mods, Side side) {
		return true;
	}
}
