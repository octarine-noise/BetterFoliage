package mods.betterfoliage.mixin;

import mods.betterfoliage.BlockModelsReloadCallback;
import net.minecraft.client.render.block.BlockModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModels.class)
public class MixinBlockModels {

    @Inject(method = "reload()V", at = @At("RETURN"))
    void onReload(CallbackInfo ci) {
        BlockModelsReloadCallback.EVENT.invoker().reloadBlockModels((BlockModels) (Object) this);
    }
}
