package mods.betterfoliage.mixin;

import mods.betterfoliage.ModelLoadingCallback;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.render.model.ModelLoader.MISSING;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Shadow @Final private ResourceManager resourceManager;

    @Inject(at = @At("HEAD"), method = "addModel")
    private void addModelHook(ModelIdentifier id, CallbackInfo info) {
        if (id == MISSING) {
            ModelLoadingCallback.EVENT.invoker().beginLoadModels((ModelLoader) (Object)this, resourceManager);
        }
    }
}
