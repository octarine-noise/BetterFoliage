package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.config.BlockConfig
import mods.betterfoliage.client.render.AsyncLogDiscovery
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.betterfoliage.client.resource.Identifier
import mods.betterfoliage.client.texture.LeafInfo
import mods.betterfoliage.client.texture.defaultRegisterLeaf
import mods.octarinecore.HasLogger
import mods.octarinecore.Map
import mods.octarinecore.ResourceLocation
import mods.octarinecore.String
import mods.octarinecore.client.resource.*
import mods.octarinecore.metaprog.*
import mods.octarinecore.metaprog.ClassRef.Companion.boolean
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.resources.IResourceManager
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.fml.ModList
import org.apache.logging.log4j.Level
import java.util.concurrent.CompletableFuture
import kotlin.collections.component1
import kotlin.collections.component2

val TextureLeaves = ClassRef<Any>("forestry.arboriculture.models.TextureLeaves")
val TextureLeaves_leafTextures = FieldRef(TextureLeaves, "leafTextures", Map)
val TextureLeaves_plain = FieldRef(TextureLeaves, "plain", ResourceLocation)
val TextureLeaves_fancy = FieldRef(TextureLeaves, "fancy", ResourceLocation)
val TextureLeaves_pollinatedPlain = FieldRef(TextureLeaves, "pollinatedPlain", ResourceLocation)
val TextureLeaves_pollinatedFancy = FieldRef(TextureLeaves, "pollinatedFancy", ResourceLocation)


val TileLeaves = ClassRef<Any>("forestry.arboriculture.tiles.TileLeaves")
val TileLeaves_getLeaveSprite = MethodRef(TileLeaves, "getLeaveSprite", ResourceLocation, boolean)
val PropertyWoodType = ClassRef<Any>("forestry.arboriculture.blocks.PropertyWoodType")
val IWoodType = ClassRef<Any>("forestry.api.arboriculture.IWoodType")
val IWoodType_barkTex = MethodRef(IWoodType, "getBarkTexture", String)
val IWoodType_heartTex = MethodRef(IWoodType, "getHeartTexture", String)

val PropertyTreeType = ClassRef<Any>("forestry.arboriculture.blocks.PropertyTreeType")
val IAlleleTreeSpecies = ClassRef<Any>("forestry.api.arboriculture.IAlleleTreeSpecies")
val ILeafSpriteProvider = ClassRef<Any>("forestry.api.arboriculture.ILeafSpriteProvider")
val TreeDefinition = ClassRef<Any>("forestry.arboriculture.genetics.TreeDefinition")

val IAlleleTreeSpecies_getLeafSpriteProvider = MethodRef(IAlleleTreeSpecies, "getLeafSpriteProvider", ILeafSpriteProvider)
val TreeDefinition_species = FieldRef(TreeDefinition, "species", IAlleleTreeSpecies)
val ILeafSpriteProvider_getSprite = MethodRef(ILeafSpriteProvider, "getSprite", ResourceLocation, boolean, boolean)

object ForestryIntegration {
    init {
        if (ModList.get().isLoaded("forestry") && allAvailable(TileLeaves_getLeaveSprite, IAlleleTreeSpecies_getLeafSpriteProvider, ILeafSpriteProvider_getSprite)) {
            // Just keep it inactive for now until Forestry updates
        }
    }
}

object ForestryLeafDiscovery : HasLogger, AsyncSpriteProvider<ModelBakery>, ModelRenderRegistry<LeafInfo> {
    override val logger = BetterFoliage.logDetail
    var idToValue = emptyMap<Identifier, LeafInfo>()

    override fun get(state: BlockState, world: IBlockReader, pos: BlockPos): LeafInfo? {
        // check variant property (used in decorative leaves)
        state.values.entries.find {
            PropertyTreeType.isInstance(it.key) && TreeDefinition.isInstance(it.value)
        } ?.let {
            val species = it.value[TreeDefinition_species]
            val spriteProvider = species[IAlleleTreeSpecies_getLeafSpriteProvider]()
            val textureLoc = spriteProvider[ILeafSpriteProvider_getSprite](false, Minecraft.isFancyGraphicsEnabled())
            return idToValue[textureLoc]
        }

        // extract leaf texture information from TileEntity
        val tile = world.getTileEntity(pos) ?: return null
        if (!TileLeaves.isInstance(tile)) return null
        val textureLoc = tile[TileLeaves_getLeaveSprite](Minecraft.isFancyGraphicsEnabled())
        return idToValue[textureLoc]
    }

    override fun setup(manager: IResourceManager, bakeryF: CompletableFuture<ModelBakery>, atlasFuture: AtlasFuture): StitchPhases {
        val futures = mutableMapOf<Identifier, CompletableFuture<LeafInfo>>()

        return StitchPhases(
            discovery = bakeryF.thenRunAsync {
                val allLeaves = TextureLeaves_leafTextures.getStatic()
                allLeaves.entries.forEach { (type, leaves) ->
                    log("base leaf type $type")
                    leaves!!
                    listOf(
                        leaves[TextureLeaves_plain], leaves[TextureLeaves_pollinatedPlain],
                        leaves[TextureLeaves_fancy], leaves[TextureLeaves_pollinatedFancy]
                    ).forEach { textureLocation ->
                        futures[textureLocation] = defaultRegisterLeaf(textureLocation, atlasFuture)
                    }
                }
            },
            cleanup = atlasFuture.runAfter {
                idToValue = futures.mapValues { it.value.get() }
            }
        )
    }
}

object ForestryLogDiscovery : ModelDiscovery<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail
    override fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<ColumnTextureInfo>? {
        // respect class list to avoid triggering on fences, stairs, etc.
        if (!BlockConfig.logBlocks.matchesClass(ctx.state.block)) return null

        // find wood type property
        val woodType = ctx.state.values.entries.find {
            PropertyWoodType.isInstance(it.key) && IWoodType.isInstance(it.value)
        }
        if (woodType != null) {
            logger.log(Level.DEBUG, "ForestryLogRegistry: block state ${ctx.state}")
            logger.log(Level.DEBUG, "ForestryLogRegistry:     variant ${woodType.value}")

            // get texture names for wood type
            val bark = woodType.value[IWoodType_barkTex]()
            val heart = woodType.value[IWoodType_heartTex]()
            logger.log(Level.DEBUG, "ForestryLogSupport:    textures [heart=$heart, bark=$bark]")

            val heartSprite = atlas.sprite(heart)
            val barkSprite = atlas.sprite(bark)
            return atlas.mapAfter {
                SimpleColumnInfo(AsyncLogDiscovery.getAxis(ctx.state), heartSprite.get(), heartSprite.get(), listOf(barkSprite.get()))
            }
        }
        return null
    }
}