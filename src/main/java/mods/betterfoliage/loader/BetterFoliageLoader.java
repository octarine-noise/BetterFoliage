package mods.betterfoliage.loader;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({
    "mods.betterfoliage.loader",
    "mods.octarinecore.metaprog",
    "kotlin"
})
@IFMLLoadingPlugin.MCVersion("1.12")
@IFMLLoadingPlugin.SortingIndex(1400)
public class BetterFoliageLoader implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "mods.betterfoliage.loader.BetterFoliageTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
