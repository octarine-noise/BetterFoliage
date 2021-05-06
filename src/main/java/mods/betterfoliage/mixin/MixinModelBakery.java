package mods.betterfoliage.mixin;

import mods.betterfoliage.BetterFoliageMod;
import mods.betterfoliage.ModelDefinitionsLoadedEvent;
import mods.betterfoliage.resource.discovery.BakeWrapperManager;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(ModelBakery.class)
abstract public class MixinModelBakery {

    private static final String processLoading = "Lnet/minecraft/client/renderer/model/ModelBakery;processLoading(Lnet/minecraft/profiler/IProfiler;I)V";
    private static final String stitch = "Lnet/minecraft/client/renderer/texture/AtlasTexture;stitch(Lnet/minecraft/resources/IResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/profiler/IProfiler;I)Lnet/minecraft/client/renderer/texture/AtlasTexture$SheetData;";
    private static final String profilerSection = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V";
    private static final String getBakedModel = "Lnet/minecraft/client/renderer/model/ModelBakery;getBakedModel(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/model/IModelTransform;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/model/IBakedModel;";
    private static final String bakeModel = "Lnet/minecraft/client/renderer/model/IUnbakedModel;bakeModel(Lnet/minecraft/client/renderer/model/ModelBakery;Ljava/util/function/Function;Lnet/minecraft/client/renderer/model/IModelTransform;Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/model/IBakedModel;";

    @Inject(method = processLoading, at = @At(value = "INVOKE", target = profilerSection, ordinal = 4))
    void onBeforeTextures(IProfiler profiler, int maxMipmapLevel, CallbackInfo ci) {
        profiler.endStartSection("betterfoliage");
        BetterFoliageMod.INSTANCE.getBus().post(new ModelDefinitionsLoadedEvent(ModelBakery.class.cast(this)));
    }

    @Redirect(method = getBakedModel, at = @At(value = "INVOKE", target = bakeModel))
    IBakedModel onStoreBakedModel(
            IUnbakedModel unbaked,
            ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter,
            IModelTransform transform,
            ResourceLocation locationIn
    ) {
        return BakeWrapperManager.INSTANCE.onBake(unbaked, bakery, spriteGetter, transform, locationIn);
    }
}
