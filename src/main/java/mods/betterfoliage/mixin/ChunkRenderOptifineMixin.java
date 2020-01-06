package mods.betterfoliage.mixin;

import mods.betterfoliage.client.Hooks;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChunkRender.class)
public class ChunkRenderOptifineMixin {

    private static final String rebuildChunk = "rebuildChunk(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V";
    private static final String invokeReflector = "Lnet/optifine/reflect/Reflector;callBoolean(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Z";
    private static final String forgeBlockCanRender = "Lnet/minecraft/client/renderer/chunk/ChunkRender;FORGE_BLOCK_CAN_RENDER_IN_LAYER:Z";

    @Redirect(
            method = rebuildChunk,
            at = @At(value = "INVOKE", target = invokeReflector),
            slice = @Slice(
                    from = @At(value = "FIELD", target = forgeBlockCanRender)
            )
    )
    @SuppressWarnings("UnresolvedMixinReference")
    boolean canRenderInLayer(Object state, @Coerce Object reflector, Object[] layer) {
        return Hooks.canRenderInLayerOverride((BlockState) state, (BlockRenderLayer) layer[0]);
    }
}
