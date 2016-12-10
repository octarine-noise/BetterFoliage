package mods.betterfoliage.client.render

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.texture.GrassRegistry
import mods.betterfoliage.client.texture.IGrassRegistry
import mods.betterfoliage.client.texture.StandardGrassSupport
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.resource.TextureListModelProcessor
import mods.octarinecore.client.resource.get
import mods.octarinecore.client.resource.registerSprite
import mods.octarinecore.common.Int3
import mods.octarinecore.common.config.ConfigurableBlockMatcher
import mods.octarinecore.common.config.ModelTextureList
import mods.octarinecore.findFirst
import mods.octarinecore.tryDefault
import net.minecraft.block.BlockLog
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing.Axis
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger

class RenderLog : AbstractRenderColumn(BetterFoliageMod.MOD_ID) {

    override val moveToCutout: Boolean get() = false

    override fun isEligible(ctx: BlockContext) =
        Config.enabled && Config.roundLogs.enabled &&
        ctx.cameraDistance < Config.roundLogs.distance &&
        Config.blocks.logClasses.matchesClass(ctx.block)

    override var axisFunc = { state: IBlockState ->
        val axis = tryDefault(null) { state.getValue(BlockLog.LOG_AXIS).toString() } ?:
            state.properties.entries.find { it.key.getName().toLowerCase() == "axis" }?.let { it.value.toString() }
        when (axis) {
            "x" -> Axis.X
            "y" -> Axis.Y
            "z" -> Axis.Z
            else -> if (Config.roundLogs.defaultY) Axis.Y else null
        }
    }

    override val registry: IColumnRegistry get() = LogRegistry

    override val blockPredicate = { state: IBlockState -> Config.blocks.logClasses.matchesClass(state.block) }
    override val surroundPredicate = { state: IBlockState -> state.isOpaqueCube && !Config.blocks.logClasses.matchesClass(state.block) }

    override val connectPerpendicular: Boolean get() = Config.roundLogs.connectPerpendicular
    override val connectSolids: Boolean get() = Config.roundLogs.connectSolids
    override val lenientConnect: Boolean get() = Config.roundLogs.lenientConnect
    override val radiusLarge: Double get() = Config.roundLogs.radiusLarge
    override val radiusSmall: Double get() = Config.roundLogs.radiusSmall

}

object LogRegistry : IColumnRegistry {
    val subRegistries: MutableList<IColumnRegistry> = mutableListOf(StandardLogSupport)
    override fun get(state: IBlockState) = subRegistries.findFirst { it[state] }
}

object StandardLogSupport : TextureListModelProcessor<IColumnTextureResolver>, IColumnRegistry {

    init { MinecraftForge.EVENT_BUS.register(this) }

    override var stateToKey = mutableMapOf<IBlockState, List<String>>()
    override var stateToValue = mapOf<IBlockState, IColumnTextureResolver>()

    override val logger = BetterFoliageMod.logDetail
    override val logName = "StandardLogSupport"
    override val matchClasses: ConfigurableBlockMatcher get() = Config.blocks.logClasses
    override val modelTextures: List<ModelTextureList> get() = Config.blocks.logModels.list

    override fun processStitch(state: IBlockState, key: List<String>, atlas: TextureMap): IColumnTextureResolver? {
        val topTex = atlas.registerSprite(key[0])
        val bottomTex = atlas.registerSprite(key[1])
        val sideTex = atlas.registerSprite(key[2])
        return StaticColumnInfo(topTex, bottomTex, sideTex)
    }

    override fun get(state: IBlockState) = stateToValue[state]
}