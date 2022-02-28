package mods.betterfoliage.mixin;

import mods.betterfoliage.Hooks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override the result of {@link BlockState}.getAmbientOcclusionLightValue().
 *
 * Needed to avoid excessive darkening of Round Logs at the corners, now that they are not full blocks.
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinBlockState {

    @Shadow protected abstract BlockState asBlockState();

    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("RETURN"), cancellable = true)
    void getAmbientOcclusionValue(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (Hooks.shouldOverrideAmbientOcclusionLightValue(this.asBlockState())) {
            cir.setReturnValue(Hooks.getAmbientOcclusionLightValueOverride());
        }
    }
}
