package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import mods.octarinecore.client.resource.AsyncSpriteProviderManager;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
abstract public class ModelBakeryMixin {

    private static final String processLoading = "Lnet/minecraft/client/renderer/model/ModelBakery;processLoading(Lnet/minecraft/profiler/IProfiler;)V";
    private static final String stitch = "Lnet/minecraft/client/renderer/texture/AtlasTexture;stitch(Lnet/minecraft/resources/IResourceManager;Ljava/lang/Iterable;Lnet/minecraft/profiler/IProfiler;)Lnet/minecraft/client/renderer/texture/AtlasTexture$SheetData;";

    @Redirect(method = processLoading, at = @At(value = "INVOKE", target = stitch))
    AtlasTexture.SheetData onStitchModelTextures(AtlasTexture atlas, IResourceManager manager, Iterable<ResourceLocation> idList, IProfiler profiler) {
        return AsyncSpriteProviderManager.INSTANCE.onStitchBlockAtlas(this, atlas, manager, idList, profiler);
    }
}
