package mods.betterfoliage.mixin;

import mods.betterfoliage.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = {"net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask"})
public class MixinChunkRenderVanilla {

    private static final String compile = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;compile(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;";
    private static final String canRenderInLayer = "Lnet/minecraft/client/renderer/RenderTypeLookup;canRenderInLayer(Lnet/minecraft/block/BlockState;Lnet/minecraft/client/renderer/RenderType;)Z";

    @Redirect(method = compile, at = @At(value = "INVOKE", target = canRenderInLayer))
    boolean canRenderInLayer(BlockState state, RenderType layer) {
        return Hooks.canRenderInLayerOverride(state, layer);
    }
}
