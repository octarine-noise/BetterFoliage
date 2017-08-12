package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliageMod
import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.render.*
import mods.betterfoliage.client.texture.ILeafRegistry
import mods.betterfoliage.client.texture.LeafInfo
import mods.betterfoliage.client.texture.LeafRegistry
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.resource.ModelProcessor
import mods.octarinecore.client.resource.ModelVariant
import mods.octarinecore.client.resource.get
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import mods.octarinecore.metaprog.allAvailable
import mods.octarinecore.tryDefault
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.client.model.IModel
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level

@SideOnly(Side.CLIENT)
object ForestryIntegration {

    val TextureLeaves = ClassRef("forestry.arboriculture.models.TextureLeaves")
    val TeLleafTextures = FieldRef(TextureLeaves, "leafTextures", Refs.Map)
    val TeLplain = FieldRef(TextureLeaves, "plain", Refs.ResourceLocation)
    val TeLfancy = FieldRef(TextureLeaves, "fancy", Refs.ResourceLocation)
    val TeLpollplain = FieldRef(TextureLeaves, "pollinatedPlain", Refs.ResourceLocation)
    val TeLpollfancy = FieldRef(TextureLeaves, "pollinatedFancy", Refs.ResourceLocation)
    val TileLeaves = ClassRef("forestry.arboriculture.tiles.TileLeaves")
    val TiLgetLeaveSprite = MethodRef(TileLeaves, "getLeaveSprite", Refs.ResourceLocation, ClassRef.boolean)

    val PropertyWoodType = ClassRef("forestry.arboriculture.blocks.property.PropertyWoodType")
    val IWoodType = ClassRef("forestry.api.arboriculture.IWoodType")
    val barkTex = MethodRef(IWoodType, "getBarkTexture", Refs.String)
    val heartTex = MethodRef(IWoodType, "getHeartTexture", Refs.String)

    val PropertyTreeType = ClassRef("forestry.arboriculture.blocks.property.PropertyTreeType")
    val TreeDefinition = ClassRef("forestry.arboriculture.genetics.TreeDefinition")
    val IAlleleTreeSpecies = ClassRef("forestry.api.arboriculture.IAlleleTreeSpecies")
    val ILeafSpriteProvider = ClassRef("forestry.api.arboriculture.ILeafSpriteProvider")
    val TdSpecies = FieldRef(TreeDefinition, "species", IAlleleTreeSpecies)
    val getLeafSpriteProvider = MethodRef(IAlleleTreeSpecies, "getLeafSpriteProvider", ILeafSpriteProvider)
    val getSprite = MethodRef(ILeafSpriteProvider, "getSprite", Refs.ResourceLocation, ClassRef.boolean, ClassRef.boolean)

    init {
        if (Loader.isModLoaded("forestry") && allAvailable(TiLgetLeaveSprite, getLeafSpriteProvider, getSprite)) {
            Client.log(Level.INFO, "Forestry support initialized")
            LeafRegistry.subRegistries.add(ForestryLeavesSupport)
            LogRegistry.subRegistries.add(ForestryLogSupport)
        }
    }
}

@SideOnly(Side.CLIENT)
object ForestryLeavesSupport : ILeafRegistry {

    val textureToValue = mutableMapOf<ResourceLocation, LeafInfo>()

    init { MinecraftForge.EVENT_BUS.register(this) }

    @SubscribeEvent
    fun handlePreStitch(event: TextureStitchEvent.Pre) {
        textureToValue.clear()
        val allLeaves = ForestryIntegration.TeLleafTextures.getStatic() as Map<*, *>
        allLeaves.entries.forEach {
            Client.logDetail("ForestryLeavesSupport: base leaf type ${it.key.toString()}")
            listOf(
                ForestryIntegration.TeLplain.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLfancy.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLpollplain.get(it.value) as ResourceLocation,
                ForestryIntegration.TeLpollfancy.get(it.value) as ResourceLocation
            ).forEach { leafLocation ->
                registerLeaf(leafLocation, event.map)
            }
        }
    }

    fun registerLeaf(textureLocation: ResourceLocation, atlas: TextureMap) {
        val texture = atlas[textureLocation.toString()] ?: return
        var leafType = LeafRegistry.typeMappings.getType(texture) ?: "default"
        Client.logDetail("ForestryLeavesSupport:        texture ${texture.iconName}")
        Client.logDetail("ForestryLeavesSupport:        particle $leafType")
        val generated = atlas.registerSprite(
            Client.genLeaves.generatedResource(texture.iconName, "type" to leafType)
        )
        textureToValue[textureLocation] = LeafInfo(generated, LeafRegistry.getParticleType(texture, atlas))
    }

    override fun get(state: IBlockState, rand: Int) = null

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing, rand: Int): LeafInfo? {
        // check variant property (used in decorative leaves)
        state.properties.entries.find {
            ForestryIntegration.PropertyTreeType.isInstance(it.key) && ForestryIntegration.TreeDefinition.isInstance(it.value)
        } ?.let {
            val species = ForestryIntegration.TdSpecies.get(it.value)
            val spriteProvider = ForestryIntegration.getLeafSpriteProvider.invoke(species!!)
            val textureLoc = ForestryIntegration.getSprite.invoke(spriteProvider!!, false, Minecraft.isFancyGraphicsEnabled())
            return textureToValue[textureLoc]
        }

        // extract leaf texture information from TileEntity
        val tile = tryDefault(null) { world.getTileEntity(pos) } ?: return null
        if (!ForestryIntegration.TileLeaves.isInstance(tile)) return null
        val textureLoc = ForestryIntegration.TiLgetLeaveSprite.invoke(tile, Minecraft.isFancyGraphicsEnabled()) ?: return null
        return textureToValue[textureLoc]
    }
}

@SideOnly(Side.CLIENT)
object ForestryLogSupport : ModelProcessor<List<String>, IColumnTextureInfo>, IColumnRegistry {

    override var variants = mutableMapOf<IBlockState, MutableList<ModelVariant>>()
    override var variantToKey = mutableMapOf<ModelVariant, List<String>>()
    override var variantToValue = mapOf<ModelVariant, IColumnTextureInfo>()

    override val logger = BetterFoliageMod.logDetail

    init { MinecraftForge.EVENT_BUS.register(this) }

    override fun processModelLoad1(state: IBlockState, modelLoc: ModelResourceLocation, model: IModel) {
        // respect class list to avoid triggering on fences, stairs, etc.
        if (!Config.blocks.logClasses.matchesClass(state.block)) return

        // find wood type property
        val woodType = state.properties.entries.find {
            ForestryIntegration.PropertyWoodType.isInstance(it.key) && ForestryIntegration.IWoodType.isInstance(it.value)
        } ?: return

        logger.log(Level.DEBUG, "ForestryLogSupport: block state ${state.toString()}")
        logger.log(Level.DEBUG, "ForestryLogSupport:     variant ${woodType.value.toString()}")

        // get texture names for wood type
        val bark = ForestryIntegration.barkTex.invoke(woodType.value) as String?
        val heart = ForestryIntegration.heartTex.invoke(woodType.value) as String?

        logger.log(Level.DEBUG, "ForestryLogSupport:    textures [heart=$heart, bark=$bark]")
        if (bark != null && heart != null) putKeySingle(state, listOf(heart, bark))
    }

    override fun processStitch(variant: ModelVariant, key: List<String>, atlas: TextureMap): IColumnTextureInfo? {
        val heart = atlas[key[0]] ?: return null
        val bark = atlas[key[1]] ?: return null
        return StaticColumnInfo(StandardLogSupport.getAxis(variant.state), heart, heart, listOf(bark))
    }

    override fun get(state: IBlockState, rand: Int) = variants[state]?.let { variantToValue[it[0]] }
}