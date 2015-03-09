package mods.betterfoliage.loader.impl;

import cpw.mods.fml.relauncher.FMLInjectionData;
import mods.betterfoliage.loader.ClassRef;
import mods.betterfoliage.loader.FieldRef;
import mods.betterfoliage.loader.MethodRef;

/** Static helper class holding constants referencing Minecraft code.<br/>
 * This should be the <b>only</b> place in the mod code with code element names!
 * @author octarine-noise
 *
 */
public class CodeRefs {
    
    public static ClassRef cBlock, cRenderBlocks, cIBlockAccess, cWorldClient, cWorld, cTextureMap, cSimpleReloadableResourceManager;
    public static ClassRef cMap, cBetterFoliageClient;
    
    public static FieldRef fBlockAccess, fMapRegisteredSprites, fDomainResourceManagers;
    
    public static MethodRef mRenderBlockByRenderType, mDoVoidFogParticles;
    public static MethodRef mGetRenderTypeOverride, mOnRandomDisplayTick, mPushEntity, mGetBlockIdOverride;
    public static MethodRef mGetAmbientOcclusionLightValue, mGetAmbientOcclusionLightValueOverride;
    public static MethodRef mGetUseNeighborBrightness, mGetUseNeighborBrightnessOverride;
    public static MethodRef mShouldSideBeRendered, mShouldRenderBlockSideOverride;
    
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
        } else if ("1.7.10".equals(mcVersion)) {
            cRenderBlocks = new ClassRef("net.minecraft.client.renderer.RenderBlocks", "blm");
            cIBlockAccess = new ClassRef("net.minecraft.world.IBlockAccess", "ahl");
            cBlock = new ClassRef("net.minecraft.block.Block", "aji");
            cWorldClient = new ClassRef("net.minecraft.client.multiplayer.WorldClient", "bjf");
            cWorld =  new ClassRef("net.minecraft.world.World", "ahb");
            cTextureMap = new ClassRef("net.minecraft.client.renderer.texture.TextureMap", "bpr");
            cSimpleReloadableResourceManager = new ClassRef("net.minecraft.client.resources.SimpleReloadableResourceManager", "brg");
        }
        
        // version-independent
        cMap = new ClassRef("java.util.Map");
        cBetterFoliageClient = new ClassRef("mods.betterfoliage.client.BetterFoliageClient");
        
        fBlockAccess = new FieldRef(cRenderBlocks, "blockAccess", null, "a", cIBlockAccess);
        fMapRegisteredSprites = new FieldRef(cTextureMap, "mapRegisteredSprites", "field_110574_e", "bpr", cMap);
        fDomainResourceManagers = new FieldRef(cSimpleReloadableResourceManager, "domainResourceManagers", "field_110548_a", null, cMap);
        
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
        
        mGetBlockIdOverride = new MethodRef(new ClassRef("mods.betterfoliage.client.ShadersModIntegration"), "getBlockIdOverride", ClassRef.INT, ClassRef.INT, cBlock);
        mPushEntity = new MethodRef(new ClassRef("shadersmodcore.client.Shaders"), "pushEntity", ClassRef.VOID, cRenderBlocks, cBlock, ClassRef.INT, ClassRef.INT, ClassRef.INT);
    }
}
