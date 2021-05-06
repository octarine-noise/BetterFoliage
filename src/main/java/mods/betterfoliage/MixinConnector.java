package mods.betterfoliage;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfiguration("betterfoliage.common.mixins.json");

        try {
            Class.forName("optifine.OptiFineTransformationService");
            Mixins.addConfiguration("betterfoliage.optifine.mixins.json");
        } catch (ClassNotFoundException e) {
            Mixins.addConfiguration("betterfoliage.vanilla.mixins.json");
        }
    }
}
