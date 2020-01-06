package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.optifine.util.BlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockUtils.class)
public class MixinOptifineBlockUtils {
    private static final String shouldSideBeRenderedCached = "shouldSideBeRenderedCached(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;Lnet/optifine/render/RenderEnv;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Z";
    private static final String getVoxelShape = "Lnet/minecraft/block/BlockState;func_215702_a(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Lnet/minecraft/util/math/shapes/VoxelShape;";

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = shouldSideBeRenderedCached, at = @At(value = "INVOKE", target = getVoxelShape, ordinal = 1))
    private static VoxelShape getVoxelShapeOverride(BlockState state, IBlockReader reader, BlockPos pos, Direction dir) {
        return Hooks.getVoxelShapeOverride(state, reader, pos, dir);
    }
}
