package mods.octarinecore

import mods.betterfoliage.util.ClassRef
import mods.betterfoliage.util.ClassRef.Companion.float
import mods.betterfoliage.util.ClassRef.Companion.void
import mods.betterfoliage.util.FieldRef
import mods.betterfoliage.util.MethodRef
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.BlockModelRenderer
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IUnbakedModel
import net.minecraft.client.renderer.model.ModelBakery
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.IBlockReader
import net.minecraftforge.client.model.pipeline.BlockInfo
import net.minecraftforge.client.model.pipeline.VertexLighterFlat
import net.minecraftforge.registries.IRegistryDelegate
import java.util.Random
import java.util.function.Predicate

typealias Sprite = TextureAtlasSprite

// Java
val String = ClassRef<String>("java.lang.String")
val List = ClassRef<List<*>>("java.util.List")
val Random = ClassRef<Random>("java.util.Random")
fun <K, V> mapRef() = ClassRef<Map<K, V>>("java.util.Map")
fun <K, V> mapRefMutable() = ClassRef<MutableMap<K, V>>("java.util.Map")

// Minecraft
val IBlockReader = ClassRef<IBlockReader>("net.minecraft.world.IBlockReader")
val ILightReader = ClassRef<IBlockDisplayReader>("net.minecraft.world.IBlockDisplayReader")
val BlockState = ClassRef<BlockState>("net.minecraft.block.BlockState")
val BlockPos = ClassRef<BlockPos>("net.minecraft.util.math.BlockPos")
val Block = ClassRef<Block>("net.minecraft.block.Block")

val TextureAtlasSprite = ClassRef<TextureAtlasSprite>("net.minecraft.client.renderer.texture.TextureAtlasSprite")
val BufferBuilder = ClassRef<BufferBuilder>("net.minecraft.client.renderer.BufferBuilder")
val BufferBuilder_setSprite = MethodRef(BufferBuilder, "setSprite", void, TextureAtlasSprite)
val BufferBuilder_sVertexBuilder = FieldRef(BufferBuilder, "sVertexBuilder", SVertexBuilder)
val BlockRendererDispatcher = ClassRef<BlockRendererDispatcher>("net.minecraft.client.renderer.BlockRendererDispatcher")
val ChunkRenderCache = ClassRef<ChunkRenderCache>("net.minecraft.client.renderer.chunk.ChunkRenderCache")
val ResourceLocation = ClassRef<ResourceLocation>("net.minecraft.util.ResourceLocation")
val BakedQuad = ClassRef<BakedQuad>("net.minecraft.client.renderer.model.BakedQuad")
val BlockModelRenderer = ClassRef<BlockModelRenderer>("net.minecraft.client.renderer.BlockModelRenderer")

val VertexLighterFlat = ClassRef<VertexLighterFlat>("net.minecraftforge.client.model.pipeline.VertexLighterFlat")
val BlockInfo = ClassRef<BlockInfo>("net.minecraftforge.client.model.pipeline.BlockInfo")
val VertexLighterFlat_blockInfo = FieldRef(VertexLighterFlat, "blockInfo", BlockInfo)
val BlockInfo_shx = FieldRef(BlockInfo, "shx", float)
val BlockInfo_shy = FieldRef(BlockInfo, "shy", float)
val BlockInfo_shz = FieldRef(BlockInfo, "shz", float)

object ModelBakery : ClassRef<ModelBakery>("net.minecraft.client.renderer.model.ModelBakery") {
    val unbakedModels = FieldRef(this, "unbakedModels", mapRefMutable<ResourceLocation, IUnbakedModel>())
    val topUnbakedModels = FieldRef(this, "topUnbakedModels", mapRefMutable<ResourceLocation, IUnbakedModel>())
}

object RenderTypeLookup : ClassRef<RenderTypeLookup>("net.minecraft.client.renderer.RenderTypeLookup") {
    val blockRenderChecks = FieldRef(this, "blockRenderChecks", mapRefMutable<IRegistryDelegate<Block>, Predicate<RenderType>>())
}

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
    val getColorMultiplier = MethodRef(this, "getColorMultiplier", int, BakedQuad, BlockState, ILightReader, BlockPos, RenderEnv)
}

// Optifine shaders
object Shaders : ClassRef<Any>("net.optifine.shaders.Shaders") {
    val shaderPackLoaded = FieldRef(this, "shaderPackLoaded", boolean)
    val blockLightLevel05 = FieldRef(this, "blockLightLevel05", float)
    val blockLightLevel06 = FieldRef(this, "blockLightLevel06", float)
    val blockLightLevel08 = FieldRef(this, "blockLightLevel08", float)
}

object SVertexBuilder : ClassRef<Any>("net.optifine.shaders.SVertexBuilder") {
    val pushState = MethodRef(this, "pushEntity", void, long)
    val popState = MethodRef(this, "popEntity", void)
}

object BlockAliases : ClassRef<Any>("net.optifine.shaders.BlockAliases") {
    val getAliasBlockId = MethodRef(this, "getAliasBlockId", int, BlockState)
}



