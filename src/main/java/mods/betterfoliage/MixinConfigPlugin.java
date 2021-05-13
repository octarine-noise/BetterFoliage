package mods.betterfoliage;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    Logger logger = LogManager.getLogger(this);

    Boolean hasOptifine = null;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (hasOptifine == null) {
            hasOptifine = FabricLoader.getInstance().isModLoaded("optifabric");
            if (hasOptifine) logger.log(Level.INFO, "[BetterFoliage] Optifabric detected, applying Optifine mixins");
            else logger.log(Level.INFO, "[BetterFoliage] Optifabric not detected, applying Vanilla mixins");
        }
        if (mixinClassName.endsWith("Vanilla") && hasOptifine) return false;
        if (mixinClassName.endsWith("Optifine") && !hasOptifine) return false;
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
