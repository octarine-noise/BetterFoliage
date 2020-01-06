package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin to override the result of {@link BlockState}.getAmbientOcclusionLightValue().
 *
 * Needed to avoid excessive darkening of Round Logs at the corners, now that they are not full blocks.
 */
@Mixin(BlockState.class)
@SuppressWarnings({"UnnecessaryQualifiedMemberReference", "deprecation"})
public class BlockStateMixin {
    private static final String callFrom = "Lnet/minecraft/block/BlockState;func_215703_d(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)F";
    private static final String callTo = "Lnet/minecraft/block/Block;func_220080_a(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)F";

    @Redirect(method = callFrom, at = @At(value = "INVOKE", target = callTo))
    float getAmbientOcclusionValue(Block block, BlockState state, IBlockReader reader, BlockPos pos) {
        return Hooks.getAmbientOcclusionLightValueOverride(block.func_220080_a(state, reader, pos), state);
    }
}
