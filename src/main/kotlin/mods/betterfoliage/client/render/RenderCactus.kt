package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.octarinecore.client.render.*
import mods.octarinecore.client.resource.ModelRenderRegistryConfigurable
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.common.config.SimpleBlockMatcher
import net.minecraft.block.BlockState
import net.minecraft.block.CactusBlock
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

object StandardCactusRegistry : ModelRenderRegistryConfigurable<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail
    override val matchClasses = SimpleBlockMatcher(CactusBlock::class.java)
    override val modelTextures = listOf(ModelTextureList("block/cactus", "top", "bottom", "side"))
    override fun processModel(state: BlockState, textures: List<String>) = SimpleColumnInfo.Key(logger, Axis.Y, textures)
    init { BetterFoliage.modBus.register(this) }
}

class RenderCactus : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val cactusStemRadius = 0.4375
    val cactusArmRotation = listOf(NORTH, SOUTH, EAST, WEST).map { Rotation.rot90[it.ordinal] }

    val iconCross = iconStatic(ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_cactus"))
    val iconArm = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_cactus_arm_$idx") }

    val modelStem = model {
        horizontalRectangle(x1 = -cactusStemRadius, x2 = cactusStemRadius, z1 = -cactusStemRadius, z2 = cactusStemRadius, y = 0.5)
        .scaleUV(cactusStemRadius * 2.0)
        .let { listOf(it.flipped.move(1.0 to DOWN), it) }
        .forEach { it.setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y), edge = null)).add() }

        verticalRectangle(x1 = -0.5, z1 = cactusStemRadius, x2 = 0.5, z2 = cactusStemRadius, yBottom = -0.5, yTop = 0.5)
        .setAoShader(faceOrientedAuto(corner = cornerAo(Axis.Y), edge = null))
        .toCross(UP).addAll()
    }
    val modelCross = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = -0.5 * 1.41, yTop = 0.5 * 1.41)
        .setAoShader(edgeOrientedAuto(corner = cornerAoMaxGreen))
        .scale(1.4)
        .transformV { v ->
            val perturb = xzDisk(modelIdx) * Config.cactus.sizeVariation
            Vertex(v.xyz + (if (v.uv.u < 0.0) perturb else -perturb), v.uv, v.aoShader)
        }
        .toCross(UP).addAll()
    }
    val modelArm = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(Config.cactus.size).move(0.5 to UP)

        .setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y), edge = null))
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.cactus.hOffset) }.addAll()
    }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${iconArm.num} cactus arm textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean =
        Config.enabled && Config.cactus.enabled &&
        StandardCactusRegistry[ctx] != null

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        // get AO data
        modelRenderer.updateShading(Int3.zero, allFaces)
        val icons = StandardCactusRegistry[ctx] ?: return renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, null)

        modelRenderer.render(
            renderer,
            modelStem.model,
            Rotation.identity,
            icon = { ctx, qi, q -> when(qi) {
                0 -> icons.bottom(ctx, qi, q); 1 -> icons.top(ctx, qi, q); else -> icons.side(ctx, qi, q)
            } },
            postProcess = noPost
        )
        modelRenderer.render(
            renderer,
            modelCross[ctx.random(0)],
            Rotation.identity,
            icon = { _, _, _ -> iconCross.icon!!},
            postProcess = noPost
        )
        modelRenderer.render(
            renderer,
            modelArm[ctx.random(1)],
            cactusArmRotation[ctx.random(2) % 4],
            icon = { _, _, _ -> iconArm[ctx.random(3)]!!},
            postProcess = noPost
        )
        return true
    }
}