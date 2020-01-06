package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin overriding the {@link VoxelShape} used for the neighbor block in {@link Block}.shouldSideBeRendered().
 *
 * This way log blocks can be made to not block rendering, without altering any {@link Block} or
 * {@link BlockState} properties with potential gameplay ramifications.
 */
@Mixin(Block.class)
public class MixinBlock {
    private static final String shouldSideBeRendered = "Lnet/minecraft/block/Block;shouldSideBeRendered(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Z";
    private static final String getVoxelShape = "Lnet/minecraft/block/BlockState;func_215702_a(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)Lnet/minecraft/util/math/shapes/VoxelShape;";

    @Redirect(method = shouldSideBeRendered, at = @At(value = "INVOKE", target = getVoxelShape, ordinal = 1))
    private static VoxelShape getVoxelShapeOverride(BlockState state, IBlockReader reader, BlockPos pos, Direction dir) {
        return Hooks.getVoxelShapeOverride(state, reader, pos, dir);
    }
}
