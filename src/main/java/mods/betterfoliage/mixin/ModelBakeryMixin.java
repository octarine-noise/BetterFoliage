package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.profiler.IProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    private static final String processLoading = "Lnet/minecraft/client/renderer/model/ModelBakery;processLoading(Lnet/minecraft/profiler/IProfiler;)V";
    private static final String endStartSection = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V";

    @Inject(method = processLoading, at = @At(value = "INVOKE_STRING", target = endStartSection, args = "ldc=stitching"))
    void preStitchTextures(IProfiler profiler, CallbackInfo ci) {
        Hooks.onLoadModelDefinitions(this);
    }
}
