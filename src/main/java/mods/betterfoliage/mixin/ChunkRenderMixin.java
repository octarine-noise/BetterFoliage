package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ChunkRender.class)
public class ChunkRenderMixin {

    private static final String rebuildChunk = "rebuildChunk(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V";
    private static final String renderBlock = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/BufferBuilder;Ljava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z";

    @Redirect(method = rebuildChunk, at = @At(value = "INVOKE", target = renderBlock))
    public boolean renderBlock(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IEnviromentBlockReader reader, BufferBuilder buffer, Random random, IModelData modelData) {
        return Hooks.renderWorldBlock(dispatcher, state, pos, reader, buffer, random, modelData, MinecraftForgeClient.getRenderLayer());
    }
}
