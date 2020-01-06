package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRender.class)
public class MixinChunkRenderVanilla {

    private static final String rebuildChunk = "rebuildChunk(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V";
    private static final String canRenderInLayer = "Lnet/minecraft/block/BlockState;canRenderInLayer(Lnet/minecraft/util/BlockRenderLayer;)Z";

    @Redirect(method = rebuildChunk, at = @At(value = "INVOKE", target = canRenderInLayer))
    boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return Hooks.canRenderInLayerOverride(state, layer);
    }
}
