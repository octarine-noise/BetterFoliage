package mods.betterfoliage.loader.impl;

import java.util.Collection;

import mods.betterfoliage.loader.AbstractResolvable;
import mods.betterfoliage.loader.ClassRef;
import mods.betterfoliage.loader.FieldRef;
import mods.betterfoliage.loader.MethodRef;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.FMLInjectionData;

/** Static helper class holding constants referencing Minecraft code.<br/>
 * This should be the <b>only</b> place in the mod code with code element names!
 * @author octarine-noise
 *
 */
public class CodeRefs {
    
	// Java
	public static ClassRef cMap;
	
	// Minecraft
    public static ClassRef cBlock, cRenderBlocks, cIBlockAccess, cWorldClient, cWorld, cTextureMap, cSimpleReloadableResourceManager, cIIcon, cTextureAtlasSprite, cIMetadataSerializer;
    public static FieldRef fBlockAccess, fMapRegisteredSprites, fDomainResourceManagers, fMetadataSerializer;
    public static MethodRef mRenderBlockByRenderType, mDoVoidFogParticles, mGetAmbientOcclusionLightValue, mGetUseNeighborBrightness, mShouldSideBeRendered;
    
    // BetterFoliage
    public static ClassRef cBetterFoliageClient;
    public static MethodRef mGetRenderTypeOverride, mOnRandomDisplayTick, mGetAmbientOcclusionLightValueOverride, mGetBlockIdOverride, mGetUseNeighborBrightnessOverride, mShouldRenderBlockSideOverride;

    // ShadersMod
    public static ClassRef cShaders;
    public static MethodRef mPushEntity;
    public static FieldRef fShadersEntityData, fShadersEntityDataIndex;
    
    // Optifine
    public static ClassRef cConnectedTextures, cConnectedProperties;
    public static MethodRef mGetConnectedTexture, mGetIndexInMap;
    public static FieldRef fCTBlockProperties, fCTTileProperties,fCPTileIcons;
    
    // CLC
    public static ClassRef cCLCLoader = new ClassRef("coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin");
    
    // Feature sets
    public static Collection<AbstractResolvable<?>> optifineCTF;
    public static Collection<AbstractResolvable<?>> shaders;
    
    static {
        String mcVersion = FMLInjectionData.data()[4].toString();
        if ("1.7.2".equals(mcVersion)) {
            cRenderBlocks = new ClassRef("net.minecraft.client.renderer.RenderBlocks", "ble");
            cIBlockAccess = new ClassRef("net.minecraft.world.IBlockAccess", "afx");
            cBlock = new ClassRef("net.minecraft.block.Block", "ahu");
            cWorldClient = new ClassRef("net.minecraft.client.multiplayer.WorldClient", "biz");
            cWorld = new ClassRef("net.minecraft.world.World", "afn");
            cTextureMap = new ClassRef("net.minecraft.client.renderer.texture.TextureMap", "bpz");
            cSimpleReloadableResourceManager = new ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "bqx");
            cIIcon =  new ClassRef("net.minecraft.util.IIcon", "ps");
            cTextureAtlasSprite = new ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bpv");
            cIMetadataSerializer = new ClassRef("net.minecraft.client.resources.data.IMetadataSerializer", "brh");
        } else if ("1.7.10".equals(mcVersion)) {
            cRenderBlocks = new ClassRef("net.minecraft.client.renderer.RenderBlocks", "blm");
            cIBlockAccess = new ClassRef("net.minecraft.world.IBlockAccess", "ahl");
            cBlock = new ClassRef("net.minecraft.block.Block", "aji");
            cWorldClient = new ClassRef("net.minecraft.client.multiplayer.WorldClient", "bjf");
            cWorld =  new ClassRef("net.minecraft.world.World", "ahb");
            cTextureMap = new ClassRef("net.minecraft.client.renderer.texture.TextureMap", "bpr");
            cSimpleReloadableResourceManager = new ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "brg");
            cIIcon =  new ClassRef("net.minecraft.util.IIcon", "rf");
            cTextureAtlasSprite = new ClassRef("net.minecraft.client.renderer.texture.TextureAtlasSprite", "bqd");
            cIMetadataSerializer = new ClassRef("net.minecraft.client.resources.data.IMetadataSerializer", "brw");
        }
        
        cMap = new ClassRef("java.util.Map");
        cBetterFoliageClient = new ClassRef("mods.betterfoliage.client.BetterFoliageClient");
        
        fBlockAccess = new FieldRef(cRenderBlocks, "blockAccess", null, "a", cIBlockAccess);
        fMapRegisteredSprites = new FieldRef(cTextureMap, "mapRegisteredSprites", "field_110574_e", "bpr", cMap);
        fDomainResourceManagers = new FieldRef(cSimpleReloadableResourceManager, "domainResourceManagers", "field_110548_a", null, cMap);
        fMetadataSerializer = new FieldRef(cSimpleReloadableResourceManager, "rmMetadataSerializer", "field_110547_c", "f", cIMetadataSerializer);
        
        mRenderBlockByRenderType = new MethodRef(cRenderBlocks, "renderBlockByRenderType", null, "b", ClassRef.BOOLEAN, cBlock, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        mDoVoidFogParticles = new MethodRef(cWorldClient, "doVoidFogParticles", null, "C", ClassRef.VOID, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        mGetRenderTypeOverride = new MethodRef(cBetterFoliageClient, "getRenderTypeOverride", ClassRef.INT, cIBlockAccess, ClassRef.INT, ClassRef.INT, ClassRef.INT, cBlock, ClassRef.INT);
        mOnRandomDisplayTick = new MethodRef(cBetterFoliageClient, "onRandomDisplayTick", ClassRef.VOID, cBlock, cWorld, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        
        mGetAmbientOcclusionLightValue = new MethodRef(cBlock, "getAmbientOcclusionLightValue", "func_149685_I", "I", ClassRef.FLOAT);
        mGetAmbientOcclusionLightValueOverride = new MethodRef(cBetterFoliageClient, "getAmbientOcclusionLightValueOverride", ClassRef.FLOAT, ClassRef.FLOAT, cBlock);
        
        mGetUseNeighborBrightness = new MethodRef(cBlock, "getUseNeighborBrightness", "func_149710_n", "n", ClassRef.BOOLEAN);
        mGetUseNeighborBrightnessOverride = new MethodRef(cBetterFoliageClient, "getUseNeighborBrightnessOverride", ClassRef.BOOLEAN, ClassRef.BOOLEAN, cBlock);
        
        mShouldSideBeRendered = new MethodRef(cBlock, "shouldSideBeRendered", "func_149646_a", "a", ClassRef.BOOLEAN, cIBlockAccess, ClassRef.INT, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        mShouldRenderBlockSideOverride = new MethodRef(cBetterFoliageClient, "shouldRenderBlockSideOverride", ClassRef.BOOLEAN, ClassRef.BOOLEAN, cIBlockAccess, ClassRef.INT, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        
        mGetBlockIdOverride = new MethodRef(new ClassRef("mods.betterfoliage.client.integration.ShadersModIntegration"), "getBlockIdOverride", ClassRef.INT, ClassRef.INT, cBlock);
        cShaders = new ClassRef("shadersmodcore.client.Shaders");
        mPushEntity = new MethodRef(cShaders, "pushEntity", ClassRef.VOID, cRenderBlocks, cBlock, ClassRef.INT, ClassRef.INT, ClassRef.INT);
        fShadersEntityData = new FieldRef(cShaders, "entityData", ClassRef.INT);
        fShadersEntityDataIndex = new FieldRef(cShaders, "entityDataIndex", ClassRef.INT);
        
        cConnectedTextures = new ClassRef("ConnectedTextures");
        cConnectedProperties = new ClassRef("ConnectedProperties");
        
        mGetConnectedTexture = new MethodRef(cConnectedTextures, "getConnectedTexture", cIIcon, cIBlockAccess, cBlock, ClassRef.INT, ClassRef.INT, ClassRef.INT, ClassRef.INT, cIIcon);
        mGetIndexInMap = new MethodRef(cTextureAtlasSprite, "getIndexInMap", ClassRef.INT);
        
        // array types not supported but unnecessary anyway
        fCTBlockProperties = new FieldRef(cConnectedTextures, "blockProperties", null);
        fCTTileProperties = new FieldRef(cConnectedTextures, "tileProperties", null);
        
        fCPTileIcons = new FieldRef(cConnectedProperties, "tileIcons", null);
        
        optifineCTF = ImmutableList.<AbstractResolvable<?>>of(
        	cConnectedTextures, cConnectedProperties, mGetConnectedTexture, mGetIndexInMap, fCTBlockProperties, fCTTileProperties, fCPTileIcons
        );
        shaders = ImmutableList.<AbstractResolvable<?>>of(cShaders, fShadersEntityData, fShadersEntityDataIndex);
    }
}
