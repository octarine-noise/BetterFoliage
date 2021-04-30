package mods.betterfoliage.client.integration

import mods.betterfoliage.BetterFoliage
import mods.betterfoliage.client.render.column.ColumnTextureInfo
import mods.betterfoliage.client.render.column.SimpleColumnInfo
import mods.octarinecore.Sprite
import mods.octarinecore.client.render.CombinedContext
import mods.octarinecore.client.render.Quad
import mods.octarinecore.client.render.lighting.QuadIconResolver
import mods.octarinecore.client.resource.*
import mods.octarinecore.common.rotate
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.allAvailable
import net.minecraft.client.renderer.model.BlockModel
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.ModList
import java.util.concurrent.CompletableFuture


object IC2RubberIntegration {

    val BlockRubWood = ClassRef<Any>("ic2.core.block.BlockRubWood")

    init {
        if (ModList.get().isLoaded("ic2") && allAvailable(BlockRubWood)) {
            // keep it inactive for now until IC2 updates
//            BetterFoliage.log(Level.INFO, "IC2 rubber support initialized")
//            LogRegistry.registries.add(IC2LogDiscovery)
//            BetterFoliage.blockSprites.providers.add(IC2LogDiscovery)
        }
    }
}

// Probably unneeded, as TechReborn went Fabric-only
/*
object TechRebornRubberIntegration {

    val BlockRubberLog = ClassRef<Any>("techreborn.blocks.BlockRubberLog")

    init {
        if (ModList.get().isLoaded("techreborn") && allAvailable(BlockRubberLog)) {
            BetterFoliage.log(Level.INFO, "TechReborn rubber support initialized")
            LogRegistry.registries.add(TechRebornLogDiscovery)
            BetterFoliage.blockSprites.providers.add(TechRebornLogDiscovery)
        }
    }
}
 */

class RubberLogInfo(
    axis: Axis?,
    val spotDir: Direction,
    topTexture: Sprite,
    bottomTexture: Sprite,
    val spotTexture: Sprite,
    sideTextures: List<Sprite>
) : SimpleColumnInfo(axis, topTexture, bottomTexture, sideTextures) {

    override val side: QuadIconResolver = { ctx: CombinedContext, idx: Int, quad: Quad ->
        val worldFace = (if ((idx and 1) == 0) SOUTH else EAST).rotate(ctx.modelRotation)
        if (worldFace == spotDir) spotTexture else {
            val sideIdx = if (this.sideTextures.size > 1) (ctx.semiRandom(1) + dirToIdx[worldFace.ordinal]) % this.sideTextures.size else 0
            this.sideTextures[sideIdx]
        }
    }
}

object IC2LogDiscovery : ModelDiscovery<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail

    override fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<ColumnTextureInfo>? {
        // check for proper block class, existence of ModelBlock, and "state" blockstate property
        if (!IC2RubberIntegration.BlockRubWood.isInstance(ctx.state.block)) return null
        val blockLoc = ctx.models.firstOrNull() as Pair<BlockModel, ResourceLocation> ?: return null
        val type = ctx.state.values.entries.find { it.key.getName() == "state" }?.value?.toString() ?: return null

        // logs with no rubber spot
        if (blockLoc.derivesFrom(ResourceLocation("block/cube_column"))) {
            val axis = when(type) {
                "plain_y" -> Axis.Y
                "plain_x" -> Axis.X
                "plain_z" -> Axis.Z
                else -> null
            }
            val textureNames = listOf("end", "side").map { blockLoc.first.resolveTextureName(it) }
            if (textureNames.any { it == "missingno" }) return null
            log("IC2LogSupport: block state ${ctx.state.toString()}")
            log("IC2LogSupport:             axis=$axis, end=${textureNames[0]}, side=${textureNames[1]}")
            val endSprite = atlas.sprite(textureNames[0])
            val sideSprite = atlas.sprite(textureNames[1])
            return atlas.mapAfter {
                SimpleColumnInfo(axis, endSprite.get(), endSprite.get(), listOf(sideSprite.get()))
            }
        }

        // logs with rubber spot
        val spotDir = when(type) {
            "dry_north", "wet_north" -> NORTH
            "dry_south", "wet_south" -> SOUTH
            "dry_west", "wet_west" -> WEST
            "dry_east", "wet_east" -> EAST
            else -> null
        }
        val textureNames = listOf("up", "down", "north", "south").map { blockLoc.first.resolveTextureName(it) }
        if (textureNames.any { it == "missingno" }) return null
        log("IC2LogSupport: block state ${ctx.state.toString()}")
        log("IC2LogSupport:             spotDir=$spotDir, up=${textureNames[0]}, down=${textureNames[1]}, side=${textureNames[2]}, spot=${textureNames[3]}")
        val upSprite = atlas.sprite(textureNames[0])
        val downSprite = atlas.sprite(textureNames[1])
        val sideSprite = atlas.sprite(textureNames[2])
        val spotSprite = atlas.sprite(textureNames[3])
        return if (spotDir != null) atlas.mapAfter {
            RubberLogInfo(Axis.Y, spotDir, upSprite.get(), downSprite.get(), spotSprite.get(), listOf(sideSprite.get()))
        } else atlas.mapAfter {
            SimpleColumnInfo(Axis.Y, upSprite.get(), downSprite.get(), listOf(sideSprite.get()))
        }
    }
}

/*
object TechRebornLogDiscovery : ModelDiscovery<ColumnTextureInfo>() {
    override val logger = BetterFoliage.logDetail

    override fun processModel(ctx: ModelDiscoveryContext, atlas: AtlasFuture): CompletableFuture<ColumnTextureInfo>? {
        // check for proper block class, existence of ModelBlock
        if (!TechRebornRubberIntegration.BlockRubberLog.isInstance(ctx.state.block)) return null
        val blockLoc = ctx.models.map { it as? Pair<BlockModel, ResourceLocation> }.firstOrNull() ?: return null

        val hasSap = ctx.state.values.entries.find { it.key.getName() == "hassap" }?.value as? Boolean ?: return null
        val sapSide = ctx.state.values.entries.find { it.key.getName() == "sapside" }?.value as? Direction ?: return null

        log("$logName: block state ${ctx.state}")
        if (hasSap) {
            val textureNames = listOf("end", "side", "sapside").map { blockLoc.first.resolveTextureName(it) }
            log("$logName:             spotDir=$sapSide, end=${textureNames[0]}, side=${textureNames[2]}, spot=${textureNames[3]}")
            if (textureNames.all { it != "missingno" }) {
                val endSprite = atlas.sprite(textureNames[0])
                val sideSprite = atlas.sprite(textureNames[1])
                val sapSprite = atlas.sprite(textureNames[2])
                return atlas.mapAfter {
                    RubberLogInfo(Axis.Y, sapSide, endSprite.get(), endSprite.get(), sapSprite.get(), listOf(sideSprite.get()))
                }
            }
        } else {
            val textureNames = listOf("end", "side").map { blockLoc.first.resolveTextureName(it) }
            log("$logName:             end=${textureNames[0]}, side=${textureNames[1]}")
            if (textureNames.all { it != "missingno" }) {
                val endSprite = atlas.sprite(textureNames[0])
                val sideSprite = atlas.sprite(textureNames[1])
                return atlas.mapAfter {
                    SimpleColumnInfo(Axis.Y, endSprite.get(), endSprite.get(), listOf(sideSprite.get()))
                }
            }
        }
        return null
    }
}
 */
