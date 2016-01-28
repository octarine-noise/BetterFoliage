package mods.betterfoliage.loader

import cpw.mods.fml.relauncher.FMLInjectionData
import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef

/** Singleton object holding references to foreign code elements. */
object Refs {
    val mcVersion = FMLInjectionData.data()[4].toString()

    // Java
    val Map = ClassRef("java.util.Map")
    val List = ClassRef("java.util.List")

    // Minecraft
    val IBlockAccess = ClassRef("net.minecraft.world.IBlockAccess", "ahl")

    val Block = ClassRef("net.minecraft.block.Block", "aji")
    val getAmbientOcclusionLightValue = MethodRef(Block, "getAmbientOcclusionLightValue", "func_149685_I", "I", ClassRef.float)
    val getUseNeighborBrightness = MethodRef(Block, "getUseNeighborBrightness", "func_149710_n", "n", ClassRef.boolean)
    val shouldSideBeRendered = MethodRef(Block, "shouldSideBeRendered", "func_149646_a", "a", ClassRef.boolean, IBlockAccess, ClassRef.int, ClassRef.int, ClassRef.int, ClassRef.int)

    val RenderBlocks = ClassRef("net.minecraft.client.renderer.RenderBlocks", "blm")
    val blockAccess = FieldRef(RenderBlocks, "blockAccess", null, "a", IBlockAccess)
    val renderBlockByRenderType = MethodRef(RenderBlocks, "renderBlockByRenderType", null, "b", ClassRef.boolean, Block, ClassRef.int, ClassRef.int, ClassRef.int)

    val WorldClient = ClassRef("net.minecraft.client.multiplayer.WorldClient", "bjf")
    val doVoidFogParticles = MethodRef(WorldClient, "doVoidFogParticles", null, "C", ClassRef.void, ClassRef.int, ClassRef.int, ClassRef.int)

    val World =  ClassRef("net.minecraft.world.World", "ahb")

    val TextureMap = ClassRef("net.minecraft.client.renderer.texture.TextureMap", "bpr")
    val mapRegisteredSprites = FieldRef(TextureMap, "mapRegisteredSprites", "field_110574_e", "bpr", Map)

    val IMetadataSerializer = ClassRef("net.minecraft.client.resources.data.IMetadataSerializer", "brw")
    val SimpleReloadableResourceManager = ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "brg")
    val metadataSerializer = FieldRef(SimpleReloadableResourceManager, "rmMetadataSerializer", "field_110547_c", "f", IMetadataSerializer)

    val IIcon = ClassRef("net.minecraft.util.IIcon", "rf")
    val TextureAtlasSprite = ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bqd")

    // Better Foliage
    val BetterFoliageHooks = ClassRef("mods.betterfoliage.client.Hooks")
    val getAmbientOcclusionLightValueOverride = MethodRef(BetterFoliageHooks, "getAmbientOcclusionLightValueOverride", ClassRef.float, ClassRef.float, Block)
    val getUseNeighborBrightnessOverride = MethodRef(BetterFoliageHooks, "getUseNeighborBrightnessOverride", ClassRef.boolean, ClassRef.boolean, Block)
    val shouldRenderBlockSideOverride = MethodRef(BetterFoliageHooks, "shouldRenderBlockSideOverride", ClassRef.boolean, ClassRef.boolean, IBlockAccess, ClassRef.int, ClassRef.int, ClassRef.int, ClassRef.int)
    val getRenderTypeOverride = MethodRef(BetterFoliageHooks, "getRenderTypeOverride", ClassRef.int, IBlockAccess, ClassRef.int, ClassRef.int, ClassRef.int, Block, ClassRef.int)
    val onRandomDisplayTick = MethodRef(BetterFoliageHooks, "onRandomDisplayTick", ClassRef.void, Block, World, ClassRef.int, ClassRef.int, ClassRef.int)

    // Shaders mod
    val Shaders = ClassRef("shadersmodcore.client.Shaders")
    val pushEntity = MethodRef(Shaders, "pushEntity", ClassRef.void, RenderBlocks, Block, ClassRef.int, ClassRef.int, ClassRef.int)
    val pushEntity_I = MethodRef(Shaders, "pushEntity", ClassRef.void, ClassRef.int)
    val popEntity = MethodRef(Shaders, "popEntity", ClassRef.void)

    val ShadersModIntegration = ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration")
    val getBlockIdOverride = MethodRef(ShadersModIntegration, "getBlockIdOverride", ClassRef.int, ClassRef.int, Block)

    // Optifine
    val ConnectedTextures = ClassRef("ConnectedTextures")
    val getConnectedTexture = MethodRef(ConnectedTextures, "getConnectedTexture", IIcon, IBlockAccess, Block, ClassRef.int, ClassRef.int, ClassRef.int, ClassRef.int, IIcon)
    val CTblockProperties = FieldRef(ConnectedTextures, "blockProperties", null)
    val CTtileProperties = FieldRef(ConnectedTextures, "tileProperties", null)

    val ConnectedProperties = ClassRef("ConnectedProperties")
    val CPmatchBlocks = FieldRef(ConnectedProperties, "matchBlocks", null)
    val CPmatchTileIcons = FieldRef(ConnectedProperties, "matchTileIcons", null)
    val CPtileIcons = FieldRef(ConnectedProperties, "tileIcons", null)

    // Colored Lights Core
    val CLCLoadingPlugin = ClassRef("coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin")
}