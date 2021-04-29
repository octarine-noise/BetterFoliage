package mods.betterfoliage.mixin;

import mods.betterfoliage.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;


@Mixin(Block.class)
public class MixinBlock {
    private static final String shouldSideBeRendered = "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z";
    private static final String getVoxelShape = "Lnet/minecraft/block/BlockState;getCullingFace(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Lnet/minecraft/util/shape/VoxelShape;";
    private static final String randomDisplayTick = "randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V";

    /**
     * Override the {@link VoxelShape} used for the neighbor block in {@link Block}.shouldSideBeRendered().
     *
     * This way log blocks can be made to not block rendering, without altering any {@link Block} or
     * {@link BlockState} properties with potential gameplay ramifications.
     */
    @Redirect(method = shouldSideBeRendered, at = @At(value = "INVOKE", target = getVoxelShape, ordinal = 1))
    private static VoxelShape getVoxelShapeOverride(BlockState state, BlockView reader, BlockPos pos, Direction dir) {
        return Hooks.getVoxelShapeOverride(state, reader, pos, dir);
    }

    /**
     * Inject a callback to call for every random display tick. Used for adding custom particle effects to blocks.
     */
    @Inject(method = randomDisplayTick, at = @At("HEAD"))
    void onRandomDisplayTick(BlockState state, World world, BlockPos pos, Random rnd, CallbackInfo ci) {
//        Hooks.onRandomDisplayTick(state.getBlock(), state, world, pos, rnd);
    }
}
