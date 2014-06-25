package mods.betterfoliage.loader;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.2")
public class BetterFoliageLoader implements IFMLLoadingPlugin {

	public String[] getASMTransformerClass() {
		return new String[] {"mods.betterfoliage.loader.BetterFoliageTransformer"};
	}

	public String getModContainerClass() {
		return null;
	}

	public String getSetupClass() {
		return null;
	}

	public void injectData(Map<String, Object> data) {
	}

	public String getAccessTransformerClass() {
		return null;
	}

}
