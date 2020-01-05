package mods.octarinecore

import mods.octarinecore.metaprog.ClassRef
import mods.octarinecore.metaprog.FieldRef
import mods.octarinecore.metaprog.MethodRef
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEnviromentBlockReader
import java.util.*

// Java
val String = ClassRef<String>("java.lang.String")
val Map = ClassRef<Map<*, *>>("java.util.Map")
val List = ClassRef<List<*>>("java.util.List")
val Random = ClassRef<Random>("java.util.Random")

// Minecraft
val IBlockReader = ClassRef<IBlockReader>("net.minecraft.world.IBlockReader")
val IEnvironmentBlockReader = ClassRef<IEnviromentBlockReader>("net.minecraft.world.IEnvironmentBlockReader")
val BlockState = ClassRef<BlockState>("net.minecraft.block.state.BlockState")
val BlockPos = ClassRef<BlockPos>("net.minecraft.util.math.BlockPos")
val BlockRenderLayer = ClassRef<BlockRenderLayer>("net.minecraft.util.BlockRenderLayer")
val Block = ClassRef<Block>("net.minecraft.block.Block")
object BufferBuilder : ClassRef<BufferBuilder>("net.minecraft.client.renderer.BufferBuilder") {
    /** Optifine only */
    val sVertexBuilder = FieldRef(this, "sVertexBuilder", SVertexBuilder)
    /** Optifine only */
    val quadSprite = FieldRef(this, "quadSprite", TextureAtlasSprite)
}
val BlockRendererDispatcher = ClassRef<BlockRendererDispatcher>("net.minecraft.client.renderer.BlockRendererDispatcher")
val ChunkRenderCache = ClassRef<ChunkRenderCache>("net.minecraft.client.renderer.chunk.ChunkRenderCache")
val TextureAtlasSprite = ClassRef<TextureAtlasSprite>("net.minecraft.client.renderer.texture.TextureAtlasSprite")
val ResourceLocation = ClassRef<ResourceLocation>("net.minecraft.util.ResourceLocation")

// Optifine
val OptifineClassTransformer = ClassRef<Any>("optifine.OptiFineClassTransformer")
val BlockPosM = ClassRef<Any>("net.optifine.BlockPosM")
object ChunkCacheOF : ClassRef<Any>("net.optifine.override.ChunkCacheOF") {
    val chunkCache = FieldRef(this, "chunkCache", ChunkRenderCache)
}

object RenderEnv : ClassRef<Any>("net.optifine.render.RenderEnv") {
    val reset = MethodRef(this, "reset", void, BlockState, BlockPos)
}

// Optifine custom colors
val IColorizer = ClassRef<Any>("net.optifine.CustomColors\$IColorizer")
object CustomColors : ClassRef<Any>("net.optifine.CustomColors") {
    val getColorMultiplier = MethodRef(this, "getSmoothColorMultiplier", int, BlockState, IEnvironmentBlockReader, BlockPos, IColorizer, BlockPosM)
}

// Optifine shaders
object SVertexBuilder : ClassRef<Any>("net.optifine.shaders.SVertexBuilder") {
    val pushState = MethodRef(this, "pushEntity", void, BlockState, BlockPos, IBlockReader, BufferBuilder)
    val pushNum = MethodRef(this, "pushEntity", void, long)
    val pop = MethodRef(this, "popEntity", void)
}



