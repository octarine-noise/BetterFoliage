package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.forgeDirOffsets
import mods.octarinecore.common.forgeDirs
import mods.octarinecore.random
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.UP
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.model.data.IModelData
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderCoral : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val noise = simplexNoise()

    val coralIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_coral_$idx") }
    val crustIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_crust_$idx") }
    val coralModels = modelSet(64) { modelIdx ->
        verticalRectangle(x1 = -0.5, z1 = 0.5, x2 = 0.5, z2 = -0.5, yBottom = 0.0, yTop = 1.0)
        .scale(Config.coral.size).move(0.5 to UP)
        .toCross(UP) { it.move(xzDisk(modelIdx) * Config.coral.hOffset) }.addAll()

        val separation = random(0.01, Config.coral.vOffset)
        horizontalRectangle(x1 = -0.5, x2 = 0.5, z1 = -0.5, z2 = 0.5, y = 0.0)
        .scale(Config.coral.crustSize).move(0.5 + separation to UP).add()

        transformQ {
            it.setAoShader(faceOrientedAuto(overrideFace = UP, corner = cornerAo(Axis.Y)))
            .setFlatShader(faceOrientedAuto(overrideFace = UP, corner = cornerFlat))
        }
    }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${coralIcons.num} coral textures")
        Client.log(DEBUG, "Registered ${crustIcons.num} coral crust textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.coral.enabled &&
        (ctx.blockState(up2).material == Material.WATER || Config.coral.shallowWater) &&
        ctx.blockState(up1).material == Material.WATER &&
        BlockConfig.sand.matchesClass(ctx.block) &&
        ctx.biome.category.let { it == Biome.Category.OCEAN || it == Biome.Category.BEACH } &&
        noise[ctx.pos] < Config.coral.population

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        val baseRender = renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)
        if (!layer.isCutout) return baseRender

        modelRenderer.updateShading(Int3.zero, allFaces)

        forgeDirs.forEachIndexed { idx, face ->
            if (!ctx.isNormalCube(forgeDirOffsets[idx]) && blockContext.random(idx) < Config.coral.chance) {
                var variation = blockContext.random(6)
                modelRenderer.render(
                    renderer,
                    coralModels[variation++],
                    rotationFromUp[idx],
                    icon = { _, qi, _ -> if (qi == 4) crustIcons[variation]!! else coralIcons[variation + (qi and 1)]!!},
                    postProcess = noPost
                )
            }
        }

        return true
    }
}