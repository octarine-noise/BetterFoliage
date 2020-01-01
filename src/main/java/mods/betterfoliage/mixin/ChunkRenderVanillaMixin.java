package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ChunkRender.class)
public class ChunkRenderVanillaMixin {

    private static final String rebuildChunk = "Lnet/minecraft/client/renderer/chunk/ChunkRender;rebuildChunk(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V";
    private static final String canRenderInLayer = "Lnet/minecraft/block/BlockState;canRenderInLayer(Lnet/minecraft/util/BlockRenderLayer;)Z";

    @Redirect(method = rebuildChunk, at = @At(value = "INVOKE", target = canRenderInLayer))
    boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return Hooks.canRenderInLayerOverride(state, layer);
    }
}
