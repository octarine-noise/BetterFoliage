package mods.betterfoliage.loader.impl;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

import mods.betterfoliage.loader.ClassRef;
import mods.betterfoliage.loader.FieldRef;
import mods.betterfoliage.loader.IResolvable;
import mods.betterfoliage.loader.MethodRef;

/** Static helper class holding constants referencing Minecraft code.<br/>
 * This should be the <b>only</b> place in the mod code with code element names!
 * @author octarine-noise
 *
 */
public class CodeRefs {
    
	// Java
    public static ClassRef cMap = new ClassRef("java.util.Map");
    
 // Minecraft
    public static ClassRef cBlock = new ClassRef("net.minecraft.block.Block", "atr");
    public static ClassRef cIBlockAccess = new ClassRef("net.minecraft.world.IBlockAccess", "ard");
    public static ClassRef cIBlockState = new ClassRef("net.minecraft.block.state.IBlockState", "bec");
    public static ClassRef cBlockPos = new ClassRef("net.minecraft.util.BlockPos", "dt");
    public static ClassRef cEnumWorldBlockLayer = new ClassRef("net.minecraft.util.EnumWorldBlockLayer", "aql");
    public static ClassRef cEnumFacing = new ClassRef("net.minecraft.util.EnumFacing", "ej");
    public static MethodRef mCanRenderInLayer = new MethodRef(cBlock, "canRenderInLayer", ClassRef.BOOLEAN, cEnumWorldBlockLayer);
    public static MethodRef mGetAmbientOcclusionLightValue = new MethodRef(cBlock, "getAmbientOcclusionLightValue", "func_149685_I", "f", ClassRef.FLOAT);
    public static MethodRef mGetUseNeighborBrightness = new MethodRef(cBlock, "getUseNeighborBrightness", "func_149710_n", "q", ClassRef.BOOLEAN);
    public static MethodRef mShouldSideBeRendered = new MethodRef(cBlock, "shouldSideBeRendered", "func_149646_a", "a", ClassRef.BOOLEAN, cIBlockAccess, cBlockPos, cEnumFacing);
    
    public static ClassRef cWorld = new ClassRef("net.minecraft.world.World", "aqu");
    public static ClassRef cWorldClient = new ClassRef("net.minecraft.client.multiplayer.WorldClient", "cen");
    public static MethodRef mDoVoidFogParticles = new MethodRef(cWorldClient, "doVoidFogParticles", "func_73029_E", "b", ClassRef.VOID, ClassRef.INT, ClassRef.INT, ClassRef.INT);
    
    public static ClassRef cBlockRendererDispatcher = new ClassRef("net.minecraft.client.renderer.BlockRendererDispatcher", "cll");
    public static ClassRef cBlockModelRenderer = new ClassRef("net.minecraft.client.renderer.BlockModelRenderer", "cln");
    public static ClassRef cRenderChunk = new ClassRef("net.minecraft.client.renderer.chunk.RenderChunk", "cop");
    public static ClassRef cChunkCompileTaskGenerator = new ClassRef("net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator", "coa");
    public static ClassRef cWorldRenderer = new ClassRef("net.minecraft.client.renderer.WorldRenderer", "civ");
    public static MethodRef mRebuildChunk = new MethodRef(cRenderChunk, "rebuildChunk", "func_178581_b", "b", ClassRef.VOID, ClassRef.FLOAT, ClassRef.FLOAT, ClassRef.FLOAT, cChunkCompileTaskGenerator);
    public static MethodRef mRenderBlock = new MethodRef(cBlockRendererDispatcher, "renderBlock", "func_175018_a", "a", ClassRef.BOOLEAN, cIBlockState, cBlockPos, cIBlockAccess, cWorldRenderer);
    
    public static ClassRef cIRegistry = new ClassRef("net.minecraft.util.IRegistry", "ez");
    public static ClassRef cModelLoader = new ClassRef("net.minecraftforge.client.model.ModelLoader");
    public static FieldRef fStateModels = new FieldRef(cModelLoader, "stateModels", cMap) ;
    public static MethodRef mSetupModelRegistry = new MethodRef(cModelLoader, "setupModelRegistry", "func_177570_a", "a", cIRegistry);
    
    public static ClassRef cSimpleReloadableResourceManager = new ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "cvt");
    public static FieldRef fDomainResourceManagers = new FieldRef(cSimpleReloadableResourceManager, "domainResourceManagers", "field_110548_a", "c", cMap);
    
    // BetterFoliage
    public static ClassRef cBetterFoliageClient = new ClassRef("mods.betterfoliage.client.BetterFoliageClient");
    public static ClassRef cShadersModIntegration = new ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration");
    public static MethodRef mOnRandomDisplayTick = new MethodRef(cBetterFoliageClient, "onRandomDisplayTick", ClassRef.VOID, cWorld, cIBlockState, cBlockPos);
    public static MethodRef mOnAfterLoadModelDefinitions = new MethodRef(cBetterFoliageClient, "onAfterLoadModelDefinitions", ClassRef.VOID, cModelLoader);
    public static MethodRef mRenderWorldBlock = new MethodRef(cBetterFoliageClient, "renderWorldBlock", ClassRef.BOOLEAN, cBlockRendererDispatcher, cIBlockState, cBlockPos, cIBlockAccess, cWorldRenderer, cEnumWorldBlockLayer);
    public static MethodRef mCanRenderBlockInLayer = new MethodRef(cBetterFoliageClient, "canRenderBlockInLayer", ClassRef.BOOLEAN, cBlock, cEnumWorldBlockLayer);
    public static MethodRef mGetBlockIdOverride = new MethodRef(cShadersModIntegration, "getBlockIdOverride", ClassRef.INT, ClassRef.INT, cIBlockState);
    public static MethodRef mGetAmbientOcclusionLightValueOverride = new MethodRef(cBetterFoliageClient, "getAmbientOcclusionLightValueOverride", ClassRef.FLOAT, ClassRef.FLOAT, cBlock);
    public static MethodRef mGetUseNeighborBrightnessOverride = new MethodRef(cBetterFoliageClient, "getUseNeighborBrightnessOverride", ClassRef.BOOLEAN, ClassRef.BOOLEAN, cBlock);
    public static MethodRef mShouldRenderBlockSideOverride = new MethodRef(cBetterFoliageClient, "shouldRenderBlockSideOverride", ClassRef.BOOLEAN, ClassRef.BOOLEAN, cIBlockAccess, cBlockPos, cEnumFacing);
    
    // ShadersMod
    public static ClassRef cSVertexBuilder = new ClassRef("shadersmod.client.SVertexBuilder");
    public static FieldRef fSVertexBuilder = new FieldRef(cWorldRenderer, "sVertexBuilder", cSVertexBuilder);
    public static MethodRef mPushEntity_S = new MethodRef(cSVertexBuilder, "pushEntity", ClassRef.VOID, cIBlockState, cBlockPos, cIBlockAccess, cWorldRenderer);
    public static MethodRef mPushEntity_I = new MethodRef(cSVertexBuilder, "pushEntity", ClassRef.VOID, ClassRef.LONG);
    public static MethodRef mPopEntity = new MethodRef(cSVertexBuilder, "popEntity", ClassRef.VOID);
    
    // Optifine
    public static ClassRef cConnectedTextures, cConnectedProperties;
    public static MethodRef mGetConnectedTexture, mGetIndexInMap;
    public static FieldRef fCTBlockProperties, fCTTileProperties,fCPTileIcons;
    
    // Feature sets
//    public static Collection<IResolvable<?>> optifineCTF = ImmutableList.<IResolvable<?>>of(
//    	cConnectedTextures, cConnectedProperties, mGetConnectedTexture, mGetIndexInMap, fCTBlockProperties, fCTTileProperties, fCPTileIcons
//    );
    public static Collection<IResolvable<?>> shaders = ImmutableList.<IResolvable<?>>of(
    	cSVertexBuilder, fSVertexBuilder, mPushEntity_S, mPushEntity_I, mPopEntity
    );
}
