package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.octarinecore.client.render.*
import mods.octarinecore.client.resource.TextureListModelProcessor
import mods.octarinecore.client.resource.registerSprite
import mods.octarinecore.common.Int3
import mods.octarinecore.common.Rotation
import mods.octarinecore.common.config.SimpleBlockMatcher
import mods.octarinecore.common.config.modelTextures
import net.minecraft.block.BlockCactus
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing.*
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level

@SideOnly(Side.CLIENT)
class RenderCactus : AbstractBlockRenderingHandler(BetterFoliageMod.MOD_ID) {

    val cactusStemRadius = 0.4375
    val cactusArmRotation = listOf(NORTH, SOUTH, EAST, WEST).map { Rotation.rot90[it.ordinal] }

    val iconCross = iconStatic(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_cactus")
    val iconArm = iconSet(BetterFoliageMod.LEGACY_DOMAIN, "blocks/better_cactus_arm_%d")

    val cactusTextures: IColumnRegistry = object : TextureListModelProcessor<IColumnTextureInfo>, IColumnRegistry {

        init { MinecraftForge.EVENT_BUS.register(this) }

        override var stateToKey = mutableMapOf<IBlockState, List<String>>()
        override var stateToValue = mapOf<IBlockState, IColumnTextureInfo>()

        override val logger = BetterFoliageMod.logDetail
        override val logName = "CactusTextures"
        override val matchClasses = SimpleBlockMatcher(BlockCactus::class.java)
        override val modelTextures = listOf(
            modelTextures("block/cactus", "top", "bottom", "side")
        )

        override fun processStitch(state: IBlockState, key: List<String>, atlas: TextureMap): IColumnTextureInfo? {
            val topTex = atlas.registerSprite(key[0])
            val bottomTex = atlas.registerSprite(key[1])
            val sideTex = atlas.registerSprite(key[2])
            return StaticColumnInfo(Axis.Y, topTex, bottomTex, sideTex)
        }

        override fun get(state: IBlockState) = stateToValue[state]
    }

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

    override fun afterStitch() {
        Client.log(Level.INFO, "Registered ${iconArm.num} cactus arm textures")
    }

    override fun isEligible(ctx: BlockContext): Boolean =
        Config.enabled && Config.cactus.enabled &&
        ctx.cameraDistance < Config.cactus.distance &&
        Config.blocks.cactus.matchesClass(ctx.block)

    override fun render(ctx: BlockContext, dispatcher: BlockRendererDispatcher, renderer: VertexBuffer, layer: BlockRenderLayer): Boolean {
        // render the whole block on the cutout layer
        if (!layer.isCutout) return false

        // get AO data
        modelRenderer.updateShading(Int3.zero, allFaces)
        val icons = cactusTextures[ctx.blockState(Int3.zero)] ?: return renderWorldBlockBase(ctx, dispatcher, renderer, null)

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