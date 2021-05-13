package mods.betterfoliage.mixin;

import mods.betterfoliage.ModelLoadingCallback;
import mods.betterfoliage.resource.discovery.BakeWrapperManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Shadow @Final private ResourceManager resourceManager;

    // use the same trick fabric-api does to get around the no-mixins-in-constructors policy
    @Inject(at = @At("HEAD"), method = "addModel")
    private void addModelHook(ModelIdentifier id, CallbackInfo info) {
        if (id.getPath().equals("trident_in_hand")) {
            // last step before stitching
            ModelLoadingCallback.EVENT.invoker().beginLoadModels((ModelLoader) (Object) this, resourceManager);
        }
    }
}
