package mods.betterfoliage.mixin;

import mods.betterfoliage.resource.discovery.BakeWrapperManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(ModelLoader.class)
public class MixinModelLoaderOptifine {

    private static final String loaderBake = "Lnet/minecraft/class_1088;getBakedModel(Lnet/minecraft/class_2960;Lnet/minecraft/class_3665;Ljava/util/function/Function;)Lnet/minecraft/class_1087;";
    private static final String modelBake = "Lnet/minecraft/class_1100;method_4753(Lnet/minecraft/class_1088;Ljava/util/function/Function;Lnet/minecraft/class_3665;Lnet/minecraft/class_2960;)Lnet/minecraft/class_1087;";

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = loaderBake, at = @At(value = "INVOKE", target = modelBake), remap = false)
    BakedModel onBakeModel(
            UnbakedModel unbaked,
            ModelLoader loader,
            Function<SpriteIdentifier, Sprite> textureGetter,
            ModelBakeSettings rotationContainer,
            Identifier modelId
    ) {
        return BakeWrapperManager.INSTANCE.onBake(unbaked, loader, textureGetter, rotationContainer, modelId);
    }
}
