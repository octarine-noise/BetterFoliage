package mods.betterfoliage.mixin;

import mods.betterfoliage.ClientChunkLoadCallback;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.client.world.ClientChunkManager$ClientChunkMap"})
public class MixinClientChunkManagerChunkMap {

    private static final String onSetAndCompare = "method_20183(ILnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/WorldChunk;)Lnet/minecraft/world/chunk/WorldChunk;";

    @Inject(method = onSetAndCompare, at = @At("HEAD"))
    void onSetAndCompare(int i, WorldChunk oldChunk, WorldChunk newChunk, CallbackInfoReturnable<WorldChunk> ci) {
        ClientChunkLoadCallback.EVENT.invoker().unloadChunk(oldChunk);
    }
}
