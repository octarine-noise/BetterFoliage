package mods.betterfoliage.mixin;

import mods.betterfoliage.BetterFoliage;
import mods.octarinecore.client.resource.AsnycSpriteProviderManager;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    private static final String reload = "reload(Lnet/minecraft/resources/IFutureReloadListener$IStage;Lnet/minecraft/resources/IResourceManager;Lnet/minecraft/profiler/IProfiler;Lnet/minecraft/profiler/IProfiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;";
    private static final String stitch = "Lnet/minecraft/client/renderer/texture/AtlasTexture;stitch(Lnet/minecraft/resources/IResourceManager;Ljava/util/stream/Stream;Lnet/minecraft/profiler/IProfiler;I)Lnet/minecraft/client/renderer/texture/AtlasTexture$SheetData;";

    // ewww :S
    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "*", at = @At(value = "INVOKE", target = stitch))
    AtlasTexture.SheetData onStitchModelTextures(AtlasTexture atlas, IResourceManager manager, Stream<ResourceLocation> idStream, IProfiler profiler, int maxMipmapLevel) {
        Set<ResourceLocation> idSetIn = idStream.collect(Collectors.toSet());
        Set<ResourceLocation> idSetOut = BetterFoliage.INSTANCE.getParticleSprites().prepare(this, manager, idSetIn, profiler);
        AtlasTexture.SheetData sheetData = atlas.stitch(manager, idSetOut.stream(), profiler, maxMipmapLevel);
        return BetterFoliage.INSTANCE.getParticleSprites().finish(sheetData, profiler);
    }
}
