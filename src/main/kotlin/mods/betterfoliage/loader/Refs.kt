package mods.betterfoliage.loader

import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import net.minecraftforge.fml.relauncher.FMLInjectionData

/** Singleton object holding references to foreign code elements. */
object Refs {
    val mcVersion = FMLInjectionData.data()[4].toString()

    // Java
    val String = ClassRef("java.lang.String")
    val Map = ClassRef("java.util.Map")
    val List = ClassRef("java.util.List")
    val Random = ClassRef("java.util.Random")

    // Minecraft
    val IBlockAccess = ClassRef("net.minecraft.world.IBlockAccess", "aih")
    val IBlockState = ClassRef("net.minecraft.block.state.IBlockState", "ars")
    val BlockStateBase = ClassRef("net.minecraft.block.state.BlockStateBase", "arp")
    val BlockPos = ClassRef("net.minecraft.util.math.BlockPos", "cm")
    val MutableBlockPos = ClassRef("net.minecraft.util.math.BlockPos\$MutableBlockPos", "cm\$a")
    val BlockRenderLayer = ClassRef("net.minecraft.util.BlockRenderLayer", "ahv")
    val EnumFacing = ClassRef("net.minecraft.util.EnumFacing", "ct")

    val World = ClassRef("net.minecraft.world.World", "aid")
    val WorldClient = ClassRef("net.minecraft.client.multiplayer.WorldClient", "bln")
    val showBarrierParticles = MethodRef(WorldClient, "showBarrierParticles", "func_184153_a", "a", ClassRef.void, ClassRef.int, ClassRef.int, ClassRef.int, ClassRef.int, Random, ClassRef.boolean, MutableBlockPos)

    val Block = ClassRef("net.minecraft.block.Block", "akf")
    val StateImplementation = ClassRef("net.minecraft.block.state.BlockStateContainer\$StateImplementation", "art\$a")
    val canRenderInLayer = MethodRef(Block, "canRenderInLayer", ClassRef.boolean, IBlockState, BlockRenderLayer)
    val getAmbientOcclusionLightValue = MethodRef(StateImplementation, "getAmbientOcclusionLightValue", "func_185892_j", "j", ClassRef.float)
    val useNeighborBrightness = MethodRef(StateImplementation, "useNeighborBrightness", "func_185916_f", "f", ClassRef.boolean)
    val doesSideBlockRendering = MethodRef(StateImplementation, "doesSideBlockRendering", ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val isOpaqueCube = MethodRef(StateImplementation, "isOpaqueCube", "func_185914_p", "p", ClassRef.boolean)
    val randomDisplayTick = MethodRef(Block, "randomDisplayTick", "func_180655_c", "a", ClassRef.void, IBlockState, World, BlockPos, Random)

    val BlockModelRenderer = ClassRef("net.minecraft.client.renderer.BlockModelRenderer", "box")
    val AmbientOcclusionFace = ClassRef("net.minecraft.client.renderer.BlockModelRenderer\$AmbientOcclusionFace", "box\$b")
    val ChunkCompileTaskGenerator = ClassRef("net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator", "bqs")
    val VertexBuffer = ClassRef("net.minecraft.client.renderer.VertexBuffer", "bnt")
    val AOF_constructor = MethodRef(AmbientOcclusionFace, "<init>", ClassRef.void, BlockModelRenderer)

    val RenderChunk = ClassRef("net.minecraft.client.renderer.chunk.RenderChunk", "bqy")
    val rebuildChunk = MethodRef(RenderChunk, "rebuildChunk", "func_178581_b", "b", ClassRef.void, ClassRef.float, ClassRef.float, ClassRef.float, ChunkCompileTaskGenerator)

    val BlockRendererDispatcher = ClassRef("net.minecraft.client.renderer.BlockRendererDispatcher", "bov")
    val renderBlock = MethodRef(BlockRendererDispatcher, "renderBlock", "func_175018_a", "a", ClassRef.boolean, IBlockState, BlockPos, IBlockAccess, VertexBuffer)

    val TextureAtlasSprite = ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bwe")

    val IRegistry = ClassRef("net.minecraft.util.registry.IRegistry", "de")
    val ModelLoader = ClassRef("net.minecraftforge.client.model.ModelLoader")
    val stateModels = FieldRef(ModelLoader, "stateModels", Map)
    val setupModelRegistry = MethodRef(ModelLoader, "setupModelRegistry", "func_177570_a", "a", IRegistry)

    val IModel = ClassRef("net.minecraftforge.client.model.IModel")
    val ModelBlock = ClassRef("net.minecraft.client.renderer.block.model.ModelBlock", "bpd")
    val ResourceLocation = ClassRef("net.minecraft.util.ResourceLocation", "kn")
    val ModelResourceLocation = ClassRef("net.minecraft.client.renderer.block.model.ModelResourceLocation", "byq")
    val VanillaModelWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$VanillaModelWrapper")
    val model_VMW = FieldRef(VanillaModelWrapper, "model", ModelBlock)
    val location_VMW = FieldRef(VanillaModelWrapper, "location", ModelBlock)
//    val WeightedPartWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedPartWrapper")
//    val model_WPW = FieldRef(WeightedPartWrapper, "model", IModel)
    val WeightedRandomModel = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedRandomModel")
    val models_WRM = FieldRef(WeightedRandomModel, "models", List)
    val MultiModel = ClassRef("net.minecraftforge.client.model.MultiModel")
    val base_MM = FieldRef(MultiModel, "base", IModel)
    val WeightedBakedModel = ClassRef("net.minecraft.client.renderer.block.model.WeightedBakedModel")
    val models_WBM = FieldRef(WeightedBakedModel, "models", List)

    val resetChangedState = MethodRef(ClassRef("net.minecraftforge.common.config.Configuration"), "resetChangedState", ClassRef.void)

    // Better Foliage
    val BetterFoliageHooks = ClassRef("mods.betterfoliage.client.Hooks")
    val getAmbientOcclusionLightValueOverride = MethodRef(BetterFoliageHooks, "getAmbientOcclusionLightValueOverride", ClassRef.float, ClassRef.float, IBlockState)
    val useNeighborBrightnessOverride = MethodRef(BetterFoliageHooks, "getUseNeighborBrightnessOverride", ClassRef.boolean, ClassRef.boolean, IBlockState)
    val doesSideBlockRenderingOverride = MethodRef(BetterFoliageHooks, "doesSideBlockRenderingOverride", ClassRef.boolean, ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val isOpaqueCubeOverride = MethodRef(BetterFoliageHooks, "isOpaqueCubeOverride", ClassRef.boolean, ClassRef.boolean, IBlockState)
    val onRandomDisplayTick = MethodRef(BetterFoliageHooks, "onRandomDisplayTick", ClassRef.void, World, IBlockState, BlockPos)
    val onAfterLoadModelDefinitions = MethodRef(BetterFoliageHooks, "onAfterLoadModelDefinitions", ClassRef.void, ModelLoader)
    val onAfterBakeModels = MethodRef(BetterFoliageHooks, "onAfterBakeModels", ClassRef.void, Map)
    val renderWorldBlock = MethodRef(BetterFoliageHooks, "renderWorldBlock", ClassRef.boolean, BlockRendererDispatcher, IBlockState, BlockPos, IBlockAccess, VertexBuffer, BlockRenderLayer)
    val canRenderBlockInLayer = MethodRef(BetterFoliageHooks, "canRenderBlockInLayer", ClassRef.boolean, Block, IBlockState, BlockRenderLayer)

    // Optifine
    val OptifineClassTransformer = ClassRef("optifine.OptiFineClassTransformer")

    val getBlockId = MethodRef(BlockStateBase, "getBlockId", ClassRef.int);
    val getMetadata = MethodRef(BlockStateBase, "getMetadata", ClassRef.int);

    val RenderEnv = ClassRef("RenderEnv")
    val RenderEnv_reset = MethodRef(RenderEnv, "reset", ClassRef.void, IBlockAccess, IBlockState, BlockPos)
    val ConnectedTextures = ClassRef("ConnectedTextures")
    val getConnectedTexture = MethodRef(ConnectedTextures, "getConnectedTextureMultiPass", TextureAtlasSprite, IBlockAccess, IBlockState, BlockPos, EnumFacing, TextureAtlasSprite, RenderEnv)
    val CTblockProperties = FieldRef(ConnectedTextures, "blockProperties", null)
    val CTtileProperties = FieldRef(ConnectedTextures, "tileProperties", null)

    val ConnectedProperties = ClassRef("ConnectedProperties")
    val CPtileIcons = FieldRef(ConnectedProperties, "tileIcons", null)
    val CPmatchesBlock = MethodRef(ConnectedProperties, "matchesBlock", ClassRef.boolean, ClassRef.int, ClassRef.int)
    val CPmatchesIcon = MethodRef(ConnectedProperties, "matchesIcon", ClassRef.boolean, TextureAtlasSprite)

    // ShadersMod
    val SVertexBuilder = ClassRef("shadersmod.client.SVertexBuilder")
    val sVertexBuilder = FieldRef(VertexBuffer, "sVertexBuilder", SVertexBuilder)
    val pushEntity_state = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, IBlockState, BlockPos, IBlockAccess, VertexBuffer)
    val pushEntity_num = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, ClassRef.long)
    val popEntity = MethodRef(SVertexBuilder, "popEntity", ClassRef.void)

    val ShadersModIntegration = ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration")
    val getBlockIdOverride = MethodRef(ShadersModIntegration, "getBlockIdOverride", ClassRef.long, ClassRef.long, IBlockState)
}