package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.texture.ILeafRegistry
import mods.betterfoliage.client.texture.LeafInfo
import mods.betterfoliage.client.texture.LeafRegistry
import mods.betterfoliage.client.texture.StandardLeafSupport
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.resource.get
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.TextureStitchEvent
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

    init {
        if (Loader.isModLoaded("forestry") && allAvailable(TextureLeaves, TileLeaves)) {
            Client.log(Level.INFO, "Forestry support initialized")
            LeafRegistry.subRegistries.add(ForestryLeavesSupport)
        }
    }
}

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

    override fun get(state: IBlockState) = null

    override fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing): LeafInfo? {
        val tile = world.getTileEntity(pos) ?: return null
        if (!ForestryIntegration.TileLeaves.isInstance(tile)) return null
        val textureLoc = ForestryIntegration.TiLgetLeaveSprite.invoke(tile, Minecraft.isFancyGraphicsEnabled()) ?: return null
        return textureToValue[textureLoc]
    }
}