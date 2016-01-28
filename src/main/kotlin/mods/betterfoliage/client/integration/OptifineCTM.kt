package mods.betterfoliage.client.integration

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import mods.betterfoliage.client.Client
import mods.betterfoliage.loader.Refs
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.client.render.Int3
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.EnumFacing
import net.minecraft.util.IIcon
import org.apache.logging.log4j.Level.*

/**
 * Integration for Optifine.
 */
@Suppress("UNCHECKED_CAST")
@SideOnly(Side.CLIENT)
object OptifineCTM {

    val isAvailable = allAvailable(Refs.ConnectedTextures, Refs.ConnectedProperties, Refs.getConnectedTexture,
        Refs.CTblockProperties, Refs.CTtileProperties, Refs.CPtileIcons, Refs.CPmatchBlocks, Refs.CPmatchTileIcons)

    val connectedProperties: Iterable<Any> get() {
        val result = hashSetOf<Any>()
        (Refs.CTblockProperties.getStatic() as Array<Array<Any?>?>).forEach { cpArray ->
            cpArray?.forEach { if (it != null) result.add(it) }
        }
        (Refs.CTtileProperties.getStatic() as Array<Array<Any?>?>).forEach { cpArray ->
            cpArray?.forEach { if (it != null) result.add(it) }
        }
        return result
    }

    /** Does this ConnectedProperties instance match the given block? */
    fun Any.matchesBlock(block: Block) = (Refs.CPmatchBlocks.get(this) as IntArray?)?.let {
        Block.getIdFromBlock(block) in (it)
    } ?: false

    /** Does this ConnectedProperties instance match the given texture? */
    fun Any.matchesIcon(icon: IIcon) = (Refs.CPmatchTileIcons.get(this) as Array<IIcon>?)?.let {
        icon in it
    } ?: false

    /** Get all the CTM [IIcon]s that could possibly be used for this block. */
    fun getAllCTM(block: Block): Collection<IIcon> {
        val result = hashSetOf<IIcon>()
        connectedProperties.forEach { cp ->
            if (cp.matchesBlock(block)) {
                //Client.log(INFO, "Match for block: ${block.toString()}, CP: ${cp.toString()}")
                result.addAll(Refs.CPtileIcons.get(cp) as Array<IIcon>)
            }
        }
        return result
    }

    /** Get all the CTM [IIcon]s that could possibly be used instead of this one. */
    fun getAllCTM(icon: IIcon): Collection<IIcon> {
        val result = hashSetOf<IIcon>()
        connectedProperties.forEach { cp ->
            if (cp.matchesIcon(icon)) {
                //Client.log(INFO, "Match for icon: ${icon.iconName}, CP: ${cp.toString()}")
                result.addAll(Refs.CPtileIcons.get(cp) as Array<IIcon>)
            }
        }
        return result
    }

}