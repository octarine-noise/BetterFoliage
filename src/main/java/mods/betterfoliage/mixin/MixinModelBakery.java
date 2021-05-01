package mods.betterfoliage.mixin;

import com.google.common.collect.Maps;
import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ModelBakery.class)
abstract public class MixinModelBakery {

    private static final String processLoading = "Lnet/minecraft/client/renderer/model/ModelBakery;processLoading(Lnet/minecraft/profiler/IProfiler;I)V";
    private static final String stitch = "Lnet/minecraft/client/renderer/texture/AtlasTexture;stitch(Lnet/minecraft/resources/IResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/profiler/IProfiler;I)Lnet/minecraft/client/renderer/texture/AtlasTexture$SheetData;";
    private static final String profilerSection = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V";

    @Redirect(method = processLoading, at = @At(value = "INVOKE", target = stitch))
    AtlasTexture.SheetData onStitchModelTextures(AtlasTexture atlas, IResourceManager manager, Stream<ResourceLocation> idStream, IProfiler profiler, int maxMipmapLevel) {
        Set<ResourceLocation> idSetIn = idStream.collect(Collectors.toSet());
        Set<ResourceLocation> idSetOut = BetterFoliage.INSTANCE.getBlockSprites().prepare(this, manager, idSetIn, profiler);
        AtlasTexture.SheetData sheetData = atlas.stitch(manager, idSetOut.stream(), profiler, maxMipmapLevel);
        return BetterFoliage.INSTANCE.getBlockSprites().finish(sheetData, profiler);
    }

    @Inject(method = processLoading, at = @At(value = "INVOKE", target = profilerSection, ordinal = 4))
    void onBeforeTextures(IProfiler profiler, int maxMipmapLevel, CallbackInfo ci) {
        profiler.endStartSection("betterfoliage");
    }
}
