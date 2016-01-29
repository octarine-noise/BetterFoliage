package mods.betterfoliage.loader

import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import net.minecraftforge.fml.relauncher.FMLInjectionData

/** Singleton object holding references to foreign code elements. */
object Refs {
    val mcVersion = FMLInjectionData.data()[4].toString()

    // Java
    val Map = ClassRef("java.util.Map")
    val List = ClassRef("java.util.List")
    val Random = ClassRef("java.util.Random")

    // Minecraft
    val IBlockAccess = ClassRef("net.minecraft.world.IBlockAccess", "adq")
    val IBlockState = ClassRef("net.minecraft.block.state.IBlockState", "alz")
    val BlockStateBase = ClassRef("net.minecraft.block.state.BlockStateBase", "aly")
    val BlockPos = ClassRef("net.minecraft.util.BlockPos", "cj")
    val EnumWorldBlockLayer = ClassRef("net.minecraft.util.EnumWorldBlockLayer", "adf")
    val EnumFacing = ClassRef("net.minecraft.util.EnumFacing", "cq")

    val World = ClassRef("net.minecraft.world.World", "adm")
    val WorldClient = ClassRef("net.minecraft.client.multiplayer.WorldClient", "bdb")
    val doVoidFogParticles = MethodRef(WorldClient, "doVoidFogParticles", "func_73029_E", "b", ClassRef.void, ClassRef.int, ClassRef.int, ClassRef.int)

    val Block = ClassRef("net.minecraft.block.Block", "afh")
    val canRenderInLayer = MethodRef(Block, "canRenderInLayer", ClassRef.boolean, EnumWorldBlockLayer)
    val getAmbientOcclusionLightValue = MethodRef(Block, "getAmbientOcclusionLightValue", "func_149685_I", "f", ClassRef.float)
    val getUseNeighborBrightness = MethodRef(Block, "getUseNeighborBrightness", "func_149710_n", "q", ClassRef.boolean)
    val shouldSideBeRendered = MethodRef(Block, "shouldSideBeRendered", "func_149646_a", "a", ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val randomDisplayTick = MethodRef(Block, "randomDisplayTick", "func_180655_c", "c", ClassRef.void, World, BlockPos, IBlockState, Random)

    val BlockModelRenderer = ClassRef("net.minecraft.client.renderer.BlockModelRenderer", "bgf")
    val AmbientOcclusionFace = ClassRef("net.minecraft.client.renderer.BlockModelRenderer\$AmbientOcclusionFace", "bgf\$b")
    val ChunkCompileTaskGenerator = ClassRef("net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator", "bhn")
    val WorldRenderer = ClassRef("net.minecraft.client.renderer.WorldRenderer", "bfd")
    val AOF_constructor = MethodRef(AmbientOcclusionFace, "<init>", ClassRef.void, BlockModelRenderer)

    val RenderChunk = ClassRef("net.minecraft.client.renderer.chunk.RenderChunk", "bht")
    val rebuildChunk = MethodRef(RenderChunk, "rebuildChunk", "func_178581_b", "b", ClassRef.void, ClassRef.float, ClassRef.float, ClassRef.float, ChunkCompileTaskGenerator)

    val BlockRendererDispatcher = ClassRef("net.minecraft.client.renderer.BlockRendererDispatcher", "bgd")
    val renderBlock = MethodRef(BlockRendererDispatcher, "renderBlock", "func_175018_a", "a", ClassRef.boolean, IBlockState, BlockPos, IBlockAccess, WorldRenderer)

    //    val IMetadataSerializer = ClassRef("net.minecraft.client.resources.data.IMetadataSerializer", "brw")
    //    val SimpleReloadableResourceManager = ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "brg")
    //    val metadataSerializer = FieldRef(SimpleReloadableResourceManager, "rmMetadataSerializer", "field_110547_c", "f", IMetadataSerializer)

    val TextureAtlasSprite = ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bmi")

    val IRegistry = ClassRef("net.minecraft.util.IRegistry", "db")
    val ModelLoader = ClassRef("net.minecraftforge.client.model.ModelLoader")
    val stateModels = FieldRef(ModelLoader, "stateModels", Map)
    val setupModelRegistry = MethodRef(ModelLoader, "setupModelRegistry", "func_177570_a", "a", IRegistry)

    val IModel = ClassRef("net.minecraftforge.client.model.IModel", "")
    val ModelBlock = ClassRef("net.minecraft.client.renderer.block.model.ModelBlock", "bgl")
    val ModelResourceLocation = ClassRef("net.minecraft.client.renderer.block.model.ModelResourceLocation", "bov")
    val VanillaModelWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$VanillaModelWrapper")
    val model_VMW = FieldRef(VanillaModelWrapper, "model", ModelBlock)
    val location_VMW = FieldRef(VanillaModelWrapper, "location", ModelBlock)
    val WeightedPartWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedPartWrapper")
    val model_WPW = FieldRef(WeightedPartWrapper, "model", IModel)
    val WeightedRandomModel = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedRandomModel")
    val models_WRM = FieldRef(WeightedRandomModel, "models", List)

    // Better Foliage
    val BetterFoliageHooks = ClassRef("mods.betterfoliage.client.Hooks")
    val getAmbientOcclusionLightValueOverride = MethodRef(BetterFoliageHooks, "getAmbientOcclusionLightValueOverride", ClassRef.float, ClassRef.float, Block)
    val getUseNeighborBrightnessOverride = MethodRef(BetterFoliageHooks, "getUseNeighborBrightnessOverride", ClassRef.boolean, ClassRef.boolean, Block)
    val shouldRenderBlockSideOverride = MethodRef(BetterFoliageHooks, "shouldRenderBlockSideOverride", ClassRef.boolean, ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val onRandomDisplayTick = MethodRef(BetterFoliageHooks, "onRandomDisplayTick", ClassRef.void, World, IBlockState, BlockPos)
    val onAfterLoadModelDefinitions = MethodRef(BetterFoliageHooks, "onAfterLoadModelDefinitions", ClassRef.void, ModelLoader)
    val renderWorldBlock = MethodRef(BetterFoliageHooks, "renderWorldBlock", ClassRef.boolean, BlockRendererDispatcher, IBlockState, BlockPos, IBlockAccess, WorldRenderer, EnumWorldBlockLayer)
    val canRenderBlockInLayer = MethodRef(BetterFoliageHooks, "canRenderBlockInLayer", ClassRef.boolean, Block, EnumWorldBlockLayer)

    // Optifine
    val OptifineClassTransformer = ClassRef("optifine.OptiFineClassTransformer")

    val RenderEnv = ClassRef("RenderEnv")
    val RenderEnv_reset = MethodRef(RenderEnv, "reset", ClassRef.void, IBlockAccess, IBlockState, BlockPos)
    val ConnectedTextures = ClassRef("ConnectedTextures")
    val getConnectedTexture = MethodRef(ConnectedTextures, "getConnectedTextureMultiPass", TextureAtlasSprite, IBlockAccess, IBlockState, BlockPos, EnumFacing, TextureAtlasSprite, RenderEnv)
    val CTblockProperties = FieldRef(ConnectedTextures, "blockProperties", null)
    val CTtileProperties = FieldRef(ConnectedTextures, "tileProperties", null)

    val ConnectedProperties = ClassRef("ConnectedProperties")
    val CPtileIcons = FieldRef(ConnectedProperties, "tileIcons", null)
    val CPmatchesBlock = MethodRef(ConnectedProperties, "matchesBlock", ClassRef.boolean, BlockStateBase)
    val CPmatchesIcon = MethodRef(ConnectedProperties, "matchesIcon", ClassRef.boolean, TextureAtlasSprite)

    // ShadersMod
    val SVertexBuilder = ClassRef("shadersmod.client.SVertexBuilder")
    val sVertexBuilder = FieldRef(WorldRenderer, "sVertexBuilder", SVertexBuilder)
    val pushEntity_state = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, IBlockState, BlockPos, IBlockAccess, WorldRenderer)
    val pushEntity_num = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, ClassRef.long)
    val popEntity = MethodRef(SVertexBuilder, "popEntity", ClassRef.void)

    val ShadersModIntegration = ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration")
    val getBlockIdOverride = MethodRef(ShadersModIntegration, "getBlockIdOverride", ClassRef.long, ClassRef.long, IBlockState)
}