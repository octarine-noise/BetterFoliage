package mods.betterfoliage.loader

import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef

/** Singleton object holding references to foreign code elements. */
object Refs {
//    val mcVersion = FMLInjectionData.data()[4].toString()

    // Java
    val String = ClassRef("java.lang.String")
    val Map = ClassRef("java.util.Map")
    val List = ClassRef("java.util.List")
    val Random = ClassRef("java.util.Random")

    // Minecraft
    val IBlockReader = ClassRef("net.minecraft.world.IBlockReader")
    val IEnvironmentBlockReader = ClassRef("net.minecraft.world.IEnvironmentBlockReader")
    val BlockState = ClassRef("net.minecraft.block.state.BlockState")
    val BlockPos = ClassRef("net.minecraft.util.math.BlockPos")
    val BlockRenderLayer = ClassRef("net.minecraft.util.BlockRenderLayer")
    val Block = ClassRef("net.minecraft.block.Block")
    val BufferBuilder = ClassRef("net.minecraft.client.renderer.BufferBuilder")
    val BlockRendererDispatcher = ClassRef("net.minecraft.client.renderer.BlockRendererDispatcher")
    val ChunkCache = ClassRef("net.minecraft.client.renderer.chunk.ChunkRenderCache")
    val TextureAtlasSprite = ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite")
    val ResourceLocation = ClassRef("net.minecraft.util.ResourceLocation")

    // Optifine
    val OptifineClassTransformer = ClassRef("optifine.OptiFineClassTransformer")
    val OptifineChunkCache = ClassRef("net.optifine.override.ChunkCacheOF")
    val CCOFChunkCache = FieldRef(OptifineChunkCache, "chunkCache", ChunkCache)

    val getBlockId = MethodRef(BlockState, "getBlockId", ClassRef.int);
    val getMetadata = MethodRef(BlockState, "getMetadata", ClassRef.int);

    // Optifine
    val RenderEnv = ClassRef("net.optifine.render.RenderEnv")
    val RenderEnv_reset = MethodRef(RenderEnv, "reset", ClassRef.void, IBlockReader, BlockState, BlockPos)
    val quadSprite = FieldRef(BufferBuilder, "quadSprite", TextureAtlasSprite)
    val BlockPosM = ClassRef("net.optifine.BlockPosM")
    val IColorizer = ClassRef("net.optifine.CustomColors\$IColorizer")

    // Optifine: custom colors
    val CustomColors = ClassRef("net.optifine.CustomColors")
    val getColorMultiplier = MethodRef(CustomColors, "getSmoothColorMultiplier", ClassRef.int, BlockState, IEnvironmentBlockReader, BlockPos, IColorizer, BlockPosM)

    // Optifine: shaders
    val SVertexBuilder = ClassRef("net.optifine.shaders.SVertexBuilder")
    val sVertexBuilder = FieldRef(BufferBuilder, "sVertexBuilder", SVertexBuilder)
    val pushEntity_state = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, BlockState, BlockPos, IBlockReader, BufferBuilder)
    val pushEntity_num = MethodRef(SVertexBuilder, "pushEntity", ClassRef.void, ClassRef.long)
    val popEntity = MethodRef(SVertexBuilder, "popEntity", ClassRef.void)

    val ShadersModIntegration = ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration")
    val getBlockIdOverride = MethodRef(ShadersModIntegration, "getBlockIdOverride", ClassRef.long, ClassRef.long, BlockState)
}