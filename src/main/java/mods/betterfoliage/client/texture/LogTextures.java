package mods.betterfoliage.client.texture;

import java.util.Collection;
import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.event.PostLoadModelDefinitionsEvent;
import mods.betterfoliage.client.texture.models.IModelTextureMapping;
import mods.betterfoliage.client.util.BFFunctions;
import mods.betterfoliage.client.util.MiscUtils;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SideOnly(Side.CLIENT)
public class LogTextures {

    /** Rendering information for a log block 
     * @author octarine-noise
     */
    public static class LogInfo {
        public LogInfo(String sideTextureName, String endTextureName) {
            this.sideTextureName = sideTextureName;
            this.endTextureName = endTextureName;
        }
        public String sideTextureName, endTextureName;
        public TextureAtlasSprite sideTexture, endTexture;
        public EnumFacing verticalDir;
    }
    
    /** {@link TextureMap} used in {@link ModelLoader} in the current run */
    public TextureMap blockTextures;
    
    public Collection<IModelTextureMapping> logSideMappings = Lists.newLinkedList();
    public Collection<IModelTextureMapping> logEndMappings = Lists.newLinkedList();
    
    /** Grass block rendering information */
    public Map<IBlockState, LogInfo> logInfoMap = Maps.newHashMap();
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void handlePostLoadModelDefinitions(PostLoadModelDefinitionsEvent event) {
        blockTextures = event.loader.textureMap;
        logInfoMap.clear();
        
        Map<ModelResourceLocation, IModel> stateModels = CodeRefs.fStateModels.getInstanceField(event.loader);
        
        Iterable<Map.Entry<IBlockState, ModelResourceLocation>> stateMappings =
        Iterables.concat(
            Iterables.transform(
                Iterables.transform(
                    Block.blockRegistry,
                    BFFunctions.getBlockStateMappings(event.loader.blockModelShapes.getBlockStateMapper().blockStateMap)),
                BFFunctions.<IBlockState, ModelResourceLocation>asEntries()
            )
        );
        
        for (Map.Entry<IBlockState, ModelResourceLocation> stateMapping : stateMappings) {
            if (logInfoMap.containsKey(stateMapping.getKey())) continue;
            if (!Config.logs.matchesClass(stateMapping.getKey().getBlock())) continue;
            
            // this is a blockstate for a log block, try to find out the textures
            IModel model = stateModels.get(stateMapping.getValue());
            
            String sideName = null, endName = null;
            for (IModelTextureMapping sideMapping : logSideMappings) if (sideName == null) sideName = sideMapping.apply(model);
            for (IModelTextureMapping endMapping : logEndMappings) if (endName == null) endName = endMapping.apply(model);
            
            if (sideName != null && endName != null) {
            	// store texture locations for this blockstate
            	LogInfo info = new LogInfo(sideName, endName);
            	logInfoMap.put(stateMapping.getKey(), info);
            	
            	// determine axis
            	Object axisValue = MiscUtils.getState(stateMapping.getKey(), "axis");
            	if (axisValue == null) info.verticalDir = EnumFacing.UP;
            	else if (axisValue.toString().toUpperCase().equals("X")) info.verticalDir = EnumFacing.EAST;
            	else if (axisValue.toString().toUpperCase().equals("Z")) info.verticalDir = EnumFacing.SOUTH;
            	else info.verticalDir = EnumFacing.UP;
            	
            	BetterFoliage.log.debug(String.format("block=%s, side=%s, end=%s, dir=%s", stateMapping.getKey().toString(), sideName, endName, info.verticalDir.toString()));
            }
        }
    }
    
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        if (event.map != blockTextures) return;
        
        for (Map.Entry<IBlockState, LogInfo> entry : logInfoMap.entrySet()) {
        	ResourceLocation sideTextureLocation = new ResourceLocation(entry.getValue().sideTextureName);
        	ResourceLocation endTextureLocation = new ResourceLocation(entry.getValue().endTextureName);
            entry.getValue().sideTexture = blockTextures.getTextureExtry(sideTextureLocation.toString());
            entry.getValue().endTexture = blockTextures.getTextureExtry(endTextureLocation.toString());
        }
    }
    
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        if (event.map != blockTextures) return;
        blockTextures = null;
    }
}
