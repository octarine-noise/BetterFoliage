package mods.betterfoliage.client.texture

import mods.betterfoliage.client.Client
import mods.betterfoliage.client.config.Config
import mods.betterfoliage.client.integration.OptifineCTM
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.resource.BlockTextureInspector
import mods.octarinecore.client.resource.IconSet
import mods.octarinecore.client.resource.averageColor
import mods.octarinecore.common.Int3
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO
import org.apache.logging.log4j.Level.WARN

const val defaultLeafColor = 0

/** Rendering-related information for a leaf block. */
class LeafInfo(
    /** The generated round leaf texture. */
    val roundLeafTexture: TextureAtlasSprite,

    /** Type of the leaf block (configurable by user). */
    val leafType: String,

    /** Average color of the round leaf texture. */
    val averageColor: Int = roundLeafTexture.averageColor ?: defaultLeafColor
) {
    /** [IconSet] of the textures to use for leaf particles emitted from this block. */
    val particleTextures: IconSet get() = LeafRegistry.particles[leafType]!!
}

/** Collects and manages rendering-related information for leaf blocks. */
@SideOnly(Side.CLIENT)
object LeafRegistry : BlockTextureInspector<TextureAtlasSprite>() {

    val leaves: MutableMap<TextureAtlasSprite, LeafInfo> = hashMapOf()
    val particles: MutableMap<String, IconSet> = hashMapOf()
    val typeMappings = TextureMatcher()

    init {
        matchClassAndModel(Config.blocks.leaves, "minecraft:block/leaves", listOf("all"))
    }

    operator fun get(state: IBlockState, world: IBlockAccess, pos: BlockPos, face: EnumFacing): LeafInfo? {
        val baseTexture = get(state) ?: return null
        return leaves[OptifineCTM.override(baseTexture, world, pos, face)] ?: leaves[baseTexture]
    }

    operator fun get(ctx: BlockContext, face: EnumFacing) = get(ctx.blockState(Int3.zero), ctx.world!!, ctx.pos, face)

    override fun onAfterModelLoad() {
        super.onAfterModelLoad()
        Client.log(INFO, "Inspecting leaf textures")
        particles.clear()
        typeMappings.loadMappings(ResourceLocation("betterfoliage", "leafTextureMappings.cfg"))
    }

    override fun processTextures(state: IBlockState, textures: List<TextureAtlasSprite>, atlas: TextureMap): TextureAtlasSprite {
        val texture = textures[0]
        registerLeaf(texture, atlas)
        OptifineCTM.getAllCTM(state, texture).forEach { registerLeaf(it, atlas) }
        return texture
    }

    fun registerLeaf(texture: TextureAtlasSprite, atlas: TextureMap) {
        var leafType = typeMappings.getType(texture) ?: "default"
        val generated = atlas.registerSprite(
            Client.genLeaves.generatedResource(texture.iconName, "type" to leafType)
        )

        if (leafType !in particles.keys) {
            val particleSet = IconSet("betterfoliage", "blocks/falling_leaf_${leafType}_%d")
            particleSet.onStitch(atlas)
            if (particleSet.num == 0) {
                Client.log(WARN, "Leaf particle textures not found for leaf type: $leafType")
                leafType == "default"
            } else {
                particles.put(leafType, particleSet)
            }
        }

        leaves[texture] = LeafInfo(generated, leafType)
    }
}