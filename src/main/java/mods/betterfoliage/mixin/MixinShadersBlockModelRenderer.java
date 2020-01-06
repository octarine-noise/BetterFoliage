package mods.betterfoliage.mixin;

import mods.betterfoliage.client.integration.ShadersModIntegration;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockModelRenderer.class)
public class MixinShadersBlockModelRenderer {

    private static final String renderModel = "renderModel(Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/model/IBakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZLjava/util/Random;JLnet/minecraftforge/client/model/data/IModelData;)Z";
    private static final String pushEntity = "Lnet/optifine/shaders/SVertexBuilder;pushEntity(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/BufferBuilder;)V";

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArg(method = renderModel, at = @At(value = "INVOKE", target = pushEntity), remap = false)
    BlockState overrideBlockState(BlockState state, BlockPos pos, IEnviromentBlockReader world, BufferBuilder buffer) {
        return ShadersModIntegration.getBlockStateOverride(state, world, pos);
    }
}
