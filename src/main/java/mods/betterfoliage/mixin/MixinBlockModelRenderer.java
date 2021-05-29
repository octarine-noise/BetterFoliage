package mods.betterfoliage.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mods.betterfoliage.model.SpecialRenderModel;
import mods.betterfoliage.render.pipeline.RenderCtxVanilla;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    private static final String renderModel = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/client/renderer/model/IBakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;JILnet/minecraftforge/client/model/data/IModelData;)Z";
    private static final String renderModelFlat = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModelFlat(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/client/renderer/model/IBakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;JILnet/minecraftforge/client/model/data/IModelData;)Z";
    private static final String renderModelSmooth = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModelSmooth(Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/client/renderer/model/IBakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;ZLjava/util/Random;JILnet/minecraftforge/client/model/data/IModelData;)Z";

    @Redirect(method = renderModel, at = @At(value = "INVOKE", target = renderModelSmooth), remap = false)
    public boolean onRenderModelSmooth(BlockModelRenderer renderer, IBlockDisplayReader world, IBakedModel model, BlockState state, BlockPos pos, MatrixStack matrixStack, IVertexBuilder buffer, boolean checkSides, Random random, long rand, int combinedOverlay, IModelData modelData) {
        if (model instanceof SpecialRenderModel)
            return RenderCtxVanilla.render(renderer, world, (SpecialRenderModel) model, state, pos, matrixStack, buffer, checkSides, random, rand, combinedOverlay, modelData, true);
        else
            return renderer.renderModelSmooth(world, model, state, pos, matrixStack, buffer, checkSides, random, rand, combinedOverlay, modelData);
    }

    @Redirect(method = renderModel, at = @At(value = "INVOKE", target = renderModelFlat), remap = false)
    public boolean onRenderModelFlat(BlockModelRenderer renderer, IBlockDisplayReader world, IBakedModel model, BlockState state, BlockPos pos, MatrixStack matrixStack, IVertexBuilder buffer, boolean checkSides, Random random, long rand, int combinedOverlay, IModelData modelData) {
        if (model instanceof SpecialRenderModel)
            return RenderCtxVanilla.render(renderer, world, (SpecialRenderModel) model, state, pos, matrixStack, buffer, checkSides, random, rand, combinedOverlay, modelData, false);
        else
            return renderer.renderModelFlat(world, model, state, pos, matrixStack, buffer, checkSides, random, rand, combinedOverlay, modelData);
    }
}
