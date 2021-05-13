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
public class MixinModelLoaderVanilla {

    private static final String loaderBake = "Lnet/minecraft/client/render/model/ModelLoader;bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;";
    private static final String modelBake = "Lnet/minecraft/client/render/model/UnbakedModel;bake(Lnet/minecraft/client/render/model/ModelLoader;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedModel;";

    @Redirect(method = loaderBake, at = @At(value = "INVOKE", target = modelBake))
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

