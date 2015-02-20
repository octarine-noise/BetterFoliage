package mods.betterfoliage.loader.impl;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({"mods.betterfoliage.loader", "optifine"})
public class BetterFoliageLoader implements IFMLLoadingPlugin {

	public String[] getASMTransformerClass() {
		return new String[] {"mods.betterfoliage.loader.impl.BetterFoliageTransformer"};
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
