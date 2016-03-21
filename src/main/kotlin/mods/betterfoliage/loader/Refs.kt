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
    val IBlockAccess = ClassRef("net.minecraft.world.IBlockAccess", "ahx")
    val IBlockState = ClassRef("net.minecraft.block.state.IBlockState", "arc")
    val BlockStateBase = ClassRef("net.minecraft.block.state.BlockStateBase", "ara")
    val BlockPos = ClassRef("net.minecraft.util.math.BlockPos", "cj")
    val MutableBlockPos = ClassRef("net.minecraft.util.math.BlockPos\$MutableBlockPos", "cj\$a")
    val BlockRenderLayer = ClassRef("net.minecraft.util.BlockRenderLayer", "ahm")
    val EnumFacing = ClassRef("net.minecraft.util.EnumFacing", "cq")

    val World = ClassRef("net.minecraft.world.World", "aht")
    val WorldClient = ClassRef("net.minecraft.client.multiplayer.WorldClient", "bku")
    val showBarrierParticles = MethodRef(WorldClient, "showBarrierParticles", "func_184153_a", "a", ClassRef.void, ClassRef.int, ClassRef.int, ClassRef.int, ClassRef.int, Random, ClassRef.boolean, MutableBlockPos)

    val Block = ClassRef("net.minecraft.block.Block", "ajt")
    val StateImplementation = ClassRef("net.minecraft.block.state.BlockStateContainer\$StateImplementation", "ard\$a")
    val canRenderInLayer = MethodRef(Block, "canRenderInLayer", ClassRef.boolean, BlockRenderLayer)
    val getAmbientOcclusionLightValue = MethodRef(StateImplementation, "getAmbientOcclusionLightValue", "func_185892_j", "j", ClassRef.float)
    val useNeighborBrightness = MethodRef(StateImplementation, "useNeighborBrightness", "func_185916_f", "f", ClassRef.boolean)
    val shouldSideBeRendered = MethodRef(StateImplementation, "shouldSideBeRendered", "func_185894_c", "c", ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val doesSideBlockRendering = MethodRef(StateImplementation, "doesSideBlockRendering", ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val randomDisplayTick = MethodRef(Block, "randomDisplayTick", "func_180655_c", "c", ClassRef.void, World, BlockPos, IBlockState, Random)

    val BlockModelRenderer = ClassRef("net.minecraft.client.renderer.BlockModelRenderer", "boe")
    val AmbientOcclusionFace = ClassRef("net.minecraft.client.renderer.BlockModelRenderer\$AmbientOcclusionFace", "boe\$b")
    val ChunkCompileTaskGenerator = ClassRef("net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator", "bpz")
    val VertexBuffer = ClassRef("net.minecraft.client.renderer.VertexBuffer", "bmz")
    val AOF_constructor = MethodRef(AmbientOcclusionFace, "<init>", ClassRef.void, BlockModelRenderer)

    val RenderChunk = ClassRef("net.minecraft.client.renderer.chunk.RenderChunk", "bqf")
    val rebuildChunk = MethodRef(RenderChunk, "rebuildChunk", "func_178581_b", "b", ClassRef.void, ClassRef.float, ClassRef.float, ClassRef.float, ChunkCompileTaskGenerator)

    val BlockRendererDispatcher = ClassRef("net.minecraft.client.renderer.BlockRendererDispatcher", "boc")
    val renderBlock = MethodRef(BlockRendererDispatcher, "renderBlock", "func_175018_a", "a", ClassRef.boolean, IBlockState, BlockPos, IBlockAccess, VertexBuffer)

    val TextureAtlasSprite = ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bvh")

    val IRegistry = ClassRef("net.minecraft.util.registry.IRegistry", "db")
    val ModelLoader = ClassRef("net.minecraftforge.client.model.ModelLoader")
    val stateModels = FieldRef(ModelLoader, "stateModels", Map)
    val setupModelRegistry = MethodRef(ModelLoader, "setupModelRegistry", "func_177570_a", "a", IRegistry)

    val IModel = ClassRef("net.minecraftforge.client.model.IModel")
    val ModelBlock = ClassRef("net.minecraft.client.renderer.block.model.ModelBlock", "bok")
    val ModelResourceLocation = ClassRef("net.minecraft.client.renderer.block.model.ModelResourceLocation", "bxt")
    val VanillaModelWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$VanillaModelWrapper")
    val model_VMW = FieldRef(VanillaModelWrapper, "model", ModelBlock)
    val location_VMW = FieldRef(VanillaModelWrapper, "location", ModelBlock)
    val WeightedPartWrapper = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedPartWrapper")
    val model_WPW = FieldRef(WeightedPartWrapper, "model", IModel)
    val WeightedRandomModel = ClassRef("net.minecraftforge.client.model.ModelLoader\$WeightedRandomModel")
    val models_WRM = FieldRef(WeightedRandomModel, "models", List)

    // Better Foliage
    val BetterFoliageHooks = ClassRef("mods.betterfoliage.client.Hooks")
    val getAmbientOcclusionLightValueOverride = MethodRef(BetterFoliageHooks, "getAmbientOcclusionLightValueOverride", ClassRef.float, ClassRef.float, IBlockState)
    val useNeighborBrightnessOverride = MethodRef(BetterFoliageHooks, "getUseNeighborBrightnessOverride", ClassRef.boolean, ClassRef.boolean, IBlockState)
    val shouldRenderBlockSideOverride = MethodRef(BetterFoliageHooks, "shouldRenderBlockSideOverride", ClassRef.boolean, ClassRef.boolean, IBlockAccess, BlockPos, EnumFacing)
    val onRandomDisplayTick = MethodRef(BetterFoliageHooks, "onRandomDisplayTick", ClassRef.void, World, IBlockState, BlockPos)
    val onAfterLoadModelDefinitions = MethodRef(BetterFoliageHooks, "onAfterLoadModelDefinitions", ClassRef.void, ModelLoader)
    val renderWorldBlock = MethodRef(BetterFoliageHooks, "renderWorldBlock", ClassRef.boolean, BlockRendererDispatcher, IBlockState, BlockPos, IBlockAccess, VertexBuffer, BlockRenderLayer)
    val canRenderBlockInLayer = MethodRef(BetterFoliageHooks, "canRenderBlockInLayer", ClassRef.boolean, Block, BlockRenderLayer)

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
    val sVertexBuilder = FieldRef(VertexBuffer, "sVertexBuilder", SVertexBuilder)
    val pushEntity_state = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, IBlockState, BlockPos, IBlockAccess, VertexBuffer)
    val pushEntity_num = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, ClassRef.long)
    val popEntity = MethodRef(SVertexBuilder, "popEntity", ClassRef.void)

    val ShadersModIntegration = ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration")
    val getBlockIdOverride = MethodRef(ShadersModIntegration, "getBlockIdOverride", ClassRef.long, ClassRef.long, IBlockState)
}