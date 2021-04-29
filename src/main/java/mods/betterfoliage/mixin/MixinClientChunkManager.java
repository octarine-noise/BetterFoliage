package mods.betterfoliage.mixin;

import mods.betterfoliage.ClientChunkLoadCallback;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    private static final String onLoadChunkFromPacket = "loadChunkFromPacket(IILnet/minecraft/world/biome/source/BiomeArray;Lnet/minecraft/util/PacketByteBuf;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/world/chunk/WorldChunk;";

    @Inject(method = onLoadChunkFromPacket, at = @At(value = "RETURN", ordinal = 2))
    void onLoadChunkFromPacket(int chunkX, int chunkZ, BiomeArray biomeArray, PacketByteBuf data, CompoundTag nbt, int updatedSectionsBits, CallbackInfoReturnable<WorldChunk> ci) {
        ClientChunkLoadCallback.EVENT.invoker().loadChunk(ci.getReturnValue());
    }
}
