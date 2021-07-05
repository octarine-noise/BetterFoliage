package mods.betterfoliage.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.betterfoliage.render.pipeline.RenderCtxBase;
import mods.betterfoliage.render.pipeline.RenderCtxForge;
import mods.betterfoliage.render.pipeline.RenderCtxVanilla;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask")
public class MixinChunkRendererDispatcher {

    private static final String compile = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;compile(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;";
    private static final String getBlockState = "Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;";

    @Inject(method = compile, at = @At(value = "INVOKE", target = getBlockState), locals = LocalCapture.CAPTURE_FAILHARD)
    void onStartBlockRender(
            float p_228940_1_, float p_228940_2_, float p_228940_3_,
            ChunkRenderDispatcher.CompiledChunk p_228940_4_, RegionRenderCacheBuilder p_228940_5_,
            CallbackInfoReturnable ci,
            int i, BlockPos blockpos, BlockPos blockpos1, VisGraph visgraph, Set set,
            ChunkRenderCache chunkrendercache, MatrixStack matrixstack,
            Random random,
            BlockRendererDispatcher blockrendererdispatcher, Iterator var15,
            BlockPos blockpos2) {
        RenderCtxBase.reset(chunkrendercache, blockrendererdispatcher, blockpos2, random);
    }
}
