package mods.betterfoliage.render.pipeline

import com.mojang.blaze3d.matrix.MatrixStack
import mods.betterfoliage.chunk.BasicBlockCtx
import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.render.old.HalfBakedQuad
import mods.betterfoliage.util.Int3
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraftforge.client.model.data.IModelData
import java.util.Random

abstract class RenderCtxBase(
    world: ILightReader,
    pos: BlockPos,
    val matrixStack: MatrixStack,
    val checkSides: Boolean,
    val random: Random,
    val modelData: IModelData
) : BlockCtx by BasicBlockCtx(world, pos) {

    var hasRendered = false
    val blockModelShapes = Minecraft.getInstance().blockRendererDispatcher.blockModelShapes
    inline fun Direction?.shouldRender() = this == null || !checkSides || Block.shouldSideBeRendered(state, world, pos, this)

    protected abstract fun renderQuad(quad: HalfBakedQuad)
    abstract fun renderFallback(model: IBakedModel)

    fun render(quads: Iterable<HalfBakedQuad>) {
        quads.forEach { quad ->
            if (quad.raw.face.shouldRender()) {
                renderQuad(quad)
                hasRendered = true
            }
        }
    }

    abstract fun renderMasquerade(offset: Int3, func: ()->Unit)
}