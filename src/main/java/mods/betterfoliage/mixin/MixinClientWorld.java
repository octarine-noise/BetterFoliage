package mods.betterfoliage.mixin;

import mods.betterfoliage.ClientWorldLoadCallback;
import mods.betterfoliage.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    private static final String scheduleBlockRerenderIfNeeded = "Lnet/minecraft/client/world/ClientWorld;scheduleBlockRerenderIfNeeded(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V";
    private static final String blockDisplayTick = "Lnet/minecraft/block/Block;randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V";

    /**
     * Inject callback to get notified of client-side blockstate changes.
     * Used to invalidate caches in the {@link mods.betterfoliage.chunk.ChunkOverlayManager}
     */
    @Inject(method = scheduleBlockRerenderIfNeeded, at = @At(value = "HEAD"))
    void onClientBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        Hooks.onClientBlockChanged((ClientWorld) (Object) this, pos, oldState, newState);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void onClientWorldCreated(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, DimensionType dimensionType, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        ClientWorldLoadCallback.EVENT.invoker().loadWorld((ClientWorld) (Object) this);
    }

    /**
     * Inject a callback to call for every random display tick. Used for adding custom particle effects to blocks.
     */
    @Inject(method = "randomBlockDisplayTick", at = @At(value = "INVOKE", target = blockDisplayTick))
    void onRandomDisplayTick(int centerX, int centerY, int centerZ, int radius, Random random, Block block, BlockPos.Mutable pos, CallbackInfo ci) {
        Hooks.onRandomDisplayTick((ClientWorld) (Object) this, pos);
    }
}
