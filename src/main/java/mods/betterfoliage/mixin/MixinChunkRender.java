package mods.betterfoliage.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mods.betterfoliage.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(targets = {"net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask"})
public class MixinChunkRender {

    private static final String compile = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;compile(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;";
    private static final String renderModel = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderModel(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/ILightReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Z";
    @Redirect(method = compile, at = @At(value = "INVOKE", target = renderModel))
    public boolean renderModel(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, ILightReader reader, MatrixStack matrixStack, IVertexBuilder vertexBuilder, boolean checkSides, Random random, IModelData modelData) {
        return Hooks.renderWorldBlock(dispatcher, state, pos, reader, matrixStack, vertexBuilder, checkSides, random, modelData, MinecraftForgeClient.getRenderLayer());
    }
}
