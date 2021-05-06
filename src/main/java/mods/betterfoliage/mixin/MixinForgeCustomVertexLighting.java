package mods.betterfoliage.mixin;

import mods.betterfoliage.render.lighting.ForgeVertexLighter;
import mods.betterfoliage.render.lighting.ForgeVertexLighterAccess;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexLighterFlat.class)
abstract public class MixinForgeCustomVertexLighting implements ForgeVertexLighter, ForgeVertexLighterAccess {

    private static final String processQuad = "Lnet/minecraftforge/client/model/pipeline/VertexLighterFlat;processQuad()V";
    private static final String updateLightmap = "Lnet/minecraftforge/client/model/pipeline/VertexLighterFlat;updateLightmap([F[FFFF)V";
    private static final String updateColor = "Lnet/minecraftforge/client/model/pipeline/VertexLighterFlat;updateColor([F[FFFFFI)V";
    private static final String resetBlockInfo = "Lnet/minecraftforge/client/model/pipeline/VertexLighterFlat;resetBlockInfo()V";

    @NotNull
    public ForgeVertexLighter vertexLighter = this;

    @NotNull
    public ForgeVertexLighter getVertexLighter() {
        return vertexLighter;
    }

    public void setVertexLighter(@NotNull ForgeVertexLighter vertexLighter) {
        this.vertexLighter = vertexLighter;
    }


    @Shadow
    protected abstract void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z);
    @Shadow
    protected abstract void updateColor(float[] normal, float[] color, float x, float y, float z, float tint, int multiplier);

    @Override
    public void updateVertexLightmap(@NotNull float[] normal, @NotNull float[] lightmap, float x, float y, float z) {
        updateLightmap(normal, lightmap, x, y, z);
    }

    @Override
    public void updateVertexColor(@NotNull float[] normal, @NotNull float[] color, float x, float y, float z, float tint, int multiplier) {
        updateColor(normal, color, x, y, z, tint, multiplier);
    }


    @Redirect(method = processQuad, at = @At(value = "INVOKE", target = updateColor), remap = false)
    void onUpdateColor(VertexLighterFlat self, float[] normal, float[] color, float x, float y, float z, float tint, int multiplier) {
        vertexLighter.updateVertexColor(normal, color, x, y, z, tint, multiplier);
    }

    @Redirect(method = processQuad, at = @At(value = "INVOKE", target = updateLightmap), remap = false)
    void onUpdateLightmap(VertexLighterFlat self, float[] normal, float[] lightmap, float x, float y, float z) {
        vertexLighter.updateVertexLightmap(normal, lightmap, x, y, z);
    }

    @Inject(method = resetBlockInfo, at = @At("RETURN"), remap = false)
    void onReset(CallbackInfo ci) {
        // just in case
        vertexLighter = this;
    }
}
