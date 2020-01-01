package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.render.LogRegistry
import mods.betterfoliage.client.render.StandardLogRegistry
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.betterfoliage.client.texture.LeafInfo
import mods.betterfoliage.client.texture.LeafRegistry
import mods.betterfoliage.client.texture.StandardLeafKey
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.resource.ModelRenderKey
import mods.octarinecore.client.resource.ModelRenderRegistry
import mods.octarinecore.client.resource.ModelRenderRegistryBase
import mods.octarinecore.getTileEntitySafe
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelResourceLocation
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModList
import org.apache.logging.log4j.Level
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.emptyMap
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.listOf
import kotlin.collections.mapValues
import kotlin.collections.mutableMapOf
import kotlin.collections.set

object ForestryIntegration {

    val TextureLeaves = ClassRef("forestry.arboriculture.models.TextureLeaves")
    val TeLleafTextures = FieldRef(TextureLeaves, "leafTextures", Refs.Map)
    val TeLplain = FieldRef(TextureLeaves, "plain", Refs.ResourceLocation)
    val TeLfancy = FieldRef(TextureLeaves, "fancy", Refs.ResourceLocation)
    val TeLpollplain = FieldRef(TextureLeaves, "pollinatedPlain", Refs.ResourceLocation)
    val TeLpollfancy = FieldRef(TextureLeaves, "pollinatedFancy", Refs.ResourceLocation)
    val TileLeaves = ClassRef("forestry.arboriculture.tiles.TileLeaves")
    val TiLgetLeaveSprite = MethodRef(TileLeaves, "getLeaveSprite", Refs.ResourceLocation, ClassRef.boolean)

    val PropertyWoodType = ClassRef("forestry.arboriculture.blocks.PropertyWoodType")
    val IWoodType = ClassRef("forestry.api.arboriculture.IWoodType")
    val barkTex = MethodRef(IWoodType, "getBarkTexture", Refs.String)
    val heartTex = MethodRef(IWoodType, "getHeartTexture", Refs.String)

    val PropertyTreeType = ClassRef("forestry.arboriculture.blocks.PropertyTreeType")
    val TreeDefinition = ClassRef("forestry.arboriculture.genetics.TreeDefinition")
    val IAlleleTreeSpecies = ClassRef("forestry.api.arboriculture.IAlleleTreeSpecies")
    val ILeafSpriteProvider = ClassRef("forestry.api.arboriculture.ILeafSpriteProvider")
    val TdSpecies = FieldRef(TreeDefinition, "species", IAlleleTreeSpecies)
    val getLeafSpriteProvider = MethodRef(IAlleleTreeSpecies, "getLeafSpriteProvider", ILeafSpriteProvider)
    val getSprite = MethodRef(ILeafSpriteProvider, "getSprite", Refs.ResourceLocation, ClassRef.boolean, ClassRef.boolean)

    init {
        if (ModList.get().isLoaded("forestry") && allAvailable(TiLgetLeaveSprite, getLeafSpriteProvider, getSprite)) {
            Client.log(Level.INFO, "Forestry support initialized")
            LeafRegistry.addRegistry(ForestryLeafRegistry)
            LogRegistry.addRegistry(ForestryLogRegistry)
        }
    }
}

object ForestryLeafRegistry : ModelRenderRegistry<LeafInfo> {
    val logger = BetterFoliage.logDetail
    val textureToKey = mutableMapOf<ResourceLocation, ModelRenderKey<LeafInfo>>()
    var textureToValue = emptyMap<ResourceLocation, LeafInfo>()

    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos): LeafInfo? {
        // check variant property (used in decorative leaves)
        state.values.entries.find {
            ForestryIntegration.PropertyTreeType.isInstance(it.key) && ForestryIntegration.TreeDefinition.isInstance(it.value)
        } ?.let {
            val species = ForestryIntegration.TdSpecies.get(it.value)
            val spriteProvider = ForestryIntegration.getLeafSpriteProvider.invoke(species!!)
            val textureLoc = ForestryIntegration.getSprite.invoke(spriteProvider!!, false, Minecraft.isFancyGraphicsEnabled())
            return textureToValue[textureLoc]
        }

        // extract leaf texture information from TileEntity
        val tile = world.getTileEntity(pos) ?: return null
        if (!ForestryIntegration.TileLeaves.isInstance(tile)) return null
        val textureLoc = ForestryIntegration.TiLgetLeaveSprite.invoke(tile, Minecraft.isFancyGraphicsEnabled()) ?: return null
        return textureToValue[textureLoc]
    }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        textureToValue = emptyMap()

        val allLeaves = ForestryIntegration.TeLleafTextures.getStatic() as Map<*, *>
        allLeaves.entries.forEach {
            logger.log(Level.DEBUG, "ForestryLeavesSupport: base leaf type ${it.key.toString()}")
            listOf(
                ForestryIntegration.TeLplain.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLfancy.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLpollplain.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLpollfancy.get(it.value) as ResourceLocation
            ).forEach { textureLocation ->
                val key = StandardLeafKey(logger, textureLocation.toString()).apply { onPreStitch(event) }
                textureToKey[textureLocation] = key
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun handlePostStitch(event: TextureStitchEvent.Post) {
        textureToValue = textureToKey.mapValues { (_, key) -> key.resolveSprites(event.map) }
        textureToKey.clear()
    }
}

object ForestryLogRegistry : ModelRenderRegistryBase<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail

    override fun processModel(state: BlockState, modelLoc: ModelResourceLocation, models: List<Pair<IUnbakedModel, ResourceLocation>>): ModelRenderKey<ColumnTextureInfo>? {
        // respect class list to avoid triggering on fences, stairs, etc.
        if (!BlockConfig.logBlocks.matchesClass(state.block)) return null

        // find wood type property
        val woodType = state.values.entries.find {
            ForestryIntegration.PropertyWoodType.isInstance(it.key) && ForestryIntegration.IWoodType.isInstance(it.value)
        } ?: return null

        logger.log(Level.DEBUG, "ForestryLogRegistry: block state $state")
        logger.log(Level.DEBUG, "ForestryLogRegistry:     variant ${woodType.value}")

        // get texture names for wood type
        val bark = ForestryIntegration.barkTex.invoke(woodType.value) as String?
        val heart = ForestryIntegration.heartTex.invoke(woodType.value) as String?

        logger.log(Level.DEBUG, "ForestryLogSupport:    textures [heart=$heart, bark=$bark]")
        if (bark != null && heart != null) return SimpleColumnInfo.Key(logger, StandardLogRegistry.getAxis(state), listOf(heart, heart, bark))
        return null
    }
}