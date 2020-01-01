package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.ShadersModIntegration
import mods.octarinecore.client.render.*
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.biome.Biome
import net.minecraftforge.client.model.data.IModelData
import org.apache.logging.log4j.Level.DEBUG
import java.util.*

class RenderAlgae : AbstractBlockRenderingHandler(BetterFoliage.MOD_ID, BetterFoliage.modBus) {

    val noise = simplexNoise()

    val algaeIcons = iconSet { idx -> ResourceLocation(BetterFoliage.MOD_ID, "blocks/better_algae_$idx") }
    val algaeModels = modelSet(64) { idx -> RenderGrass.grassTopQuads(Config.algae.heightMin, Config.algae.heightMax)(idx) }

    override fun afterPreStitch() {
        Client.log(DEBUG, "Registered ${algaeIcons.num} algae textures")
    }

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.algae.enabled &&
        ctx.blockState(up2).material == Material.WATER &&
        ctx.blockState(up1).material == Material.WATER &&
        BlockTags.DIRT_LIKE.contains(ctx.block) &&
        ctx.biome.category.let { it == Biome.Category.OCEAN || it == Biome.Category.BEACH || it == Biome.Category.RIVER } &&
        noise[ctx.pos] < Config.algae.population

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: BufferBuilder, random: Random, modelData: IModelData, layer: BlockRenderLayer): Boolean {
        val baseRender = renderWorldBlockBase(ctx, dispatcher, renderer, random, modelData, layer)
        if (!layer.isCutout) return baseRender

        modelRenderer.updateShading(Int3.zero, allFaces)

        val rand = ctx.semiRandomArray(3)

        ShadersModIntegration.grass(renderer, Config.algae.shaderWind) {
            modelRenderer.render(
                renderer,
                algaeModels[rand[2]],
                Rotation.identity,
                icon = { _, qi, _ -> algaeIcons[rand[qi and 1]]!! },
                postProcess = noPost
            )
        }
        return true
    }
}