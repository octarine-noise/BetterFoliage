package mods.betterfoliage.client.integration

import mods.betterfoliage.client.Client
import mods.betterfoliage.loader.Refs
import mods.octarinecore.ThreadLocalDelegate
import mods.octarinecore.client.render.BlockContext
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.block.Block
import net.minecraft.block.state.BlockStateBase
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Level.INFO

/**
 * Integration for OptiFine.
 */
@Suppress("UNCHECKED_CAST")
@SideOnly(Side.CLIENT)
object OptifineCTM {

    val isAvailable = allAvailable(Refs.ConnectedTextures, Refs.ConnectedProperties, Refs.getConnectedTexture,
        Refs.CTblockProperties, Refs.CTtileProperties, Refs.CPtileIcons, Refs.CPmatchesBlock, Refs.CPmatchesIcon)

    init {
        Client.log(INFO, "Optifine CTM support is ${if (isAvailable) "enabled" else "disabled" }")
    }

    val renderEnv by ThreadLocalDelegate { OptifineRenderEnv() }

    val connectedProperties: Iterable<Any> get() {
        val result = hashSetOf<Any>()
        (Refs.CTblockProperties.getStatic() as Array<Array<Any?>?>?)?.forEach { cpArray ->
            cpArray?.forEach { if (it != null) result.add(it) }
        }
        (Refs.CTtileProperties.getStatic() as Array<Array<Any?>?>?)?.forEach { cpArray ->
            cpArray?.forEach { if (it != null) result.add(it) }
        }
        return result
    }

    /** Get all the CTM [TextureAtlasSprite]s that could possibly be used for this block. */
    fun getAllCTM(state: IBlockState, icon: TextureAtlasSprite): Collection<TextureAtlasSprite> {
        val result = hashSetOf<TextureAtlasSprite>()
        if (state !is BlockStateBase) return result

        connectedProperties.forEach { cp ->
            if (Refs.CPmatchesBlock.invoke(cp, state) as Boolean &&
                Refs.CPmatchesIcon.invoke(cp, icon) as Boolean) {
                Client.log(INFO, "Match for block: ${state.toString()}, icon: ${icon.iconName} -> CP: ${cp.toString()}")
                result.addAll(Refs.CPtileIcons.get(cp) as Array<TextureAtlasSprite>)
            }
        }
        return result
    }

    fun override(texture: TextureAtlasSprite, ctx: BlockContext, face: EnumFacing) =
        override(texture, ctx.world!!, ctx.pos, face)

    fun override(texture: TextureAtlasSprite, world: IBlockAccess, pos: BlockPos, face: EnumFacing): TextureAtlasSprite {
        if (!isAvailable) return texture
        val state = world.getBlockState(pos)

        return renderEnv.let {
            it.reset(world, state, pos)
            Refs.getConnectedTexture.invokeStatic(world, state, pos, face, texture, it.wrapped) as TextureAtlasSprite
        }
    }
}

class OptifineRenderEnv {
    val wrapped: Any = Refs.RenderEnv.element!!.getDeclaredConstructor(
        Refs.IBlockAccess.element, Refs.IBlockState.element, Refs.BlockPos.element
    ).let {
        it.isAccessible = true
        it.newInstance(null, null, null)
    }

    fun reset(blockAccess: IBlockAccess, state: IBlockState, pos: BlockPos) {
        Refs.RenderEnv_reset.invoke(wrapped, blockAccess, state, pos)
    }
}