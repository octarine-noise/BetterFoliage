package mods.betterfoliage.client.texture;

import java.util.Collection;
import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.event.PostLoadModelDefinitionsEvent;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.texture.models.IModelTextureMapping;
import mods.betterfoliage.client.util.BFFunctions;
import mods.betterfoliage.client.util.ResourceUtils;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
public class GrassTextures {

    /** Rendering information for a grass block 
     * @author octarine-noise
     */
    public static class GrassInfo {
        public GrassInfo(String baseTextureName) {
            this.baseTextureName = baseTextureName;
        }
        public String baseTextureName;
        public Color4 averageColor;
        public boolean useTextureColor;
    }
    
    /** {@link TextureMap} used in {@link ModelLoader} in the current run */
    public TextureMap blockTextures;
    
    public Collection<IModelTextureMapping> grassMappings = Lists.newLinkedList();
    
    /** Grass block rendering information */
    public Map<IBlockState, GrassInfo> grassInfoMap = Maps.newHashMap();
    
    public Color4 getRenderColor(IBlockState blockState, Color4 defaultColor) {
    	GrassInfo info = grassInfoMap.get(blockState);
    	return (info == null || !info.useTextureColor) ? defaultColor : info.averageColor;
    }
    
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void handlePostLoadModelDefinitions(PostLoadModelDefinitionsEvent event) {
        blockTextures = event.loader.textureMap;
        grassInfoMap.clear();
        
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
            if (grassInfoMap.containsKey(stateMapping.getKey())) continue;
            if (!Config.grass.matchesClass(stateMapping.getKey().getBlock())) continue;
            
            // this is a blockstate for a grass block, try to find out the base grass top texture
            IModel model = stateModels.get(stateMapping.getValue());
            for (IModelTextureMapping grassMapping : grassMappings) {
                String resolvedName = grassMapping.apply(model);
                if (resolvedName != null) {
                    // store texture location for this blockstate
                    BetterFoliage.log.debug(String.format("block=%s, texture=%s", stateMapping.getKey().toString(), resolvedName));
                    grassInfoMap.put(stateMapping.getKey(), new GrassInfo(resolvedName));
                    break;
                }
            }
        }
    }
    
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        if (event.map != blockTextures) return;
        
        for (Map.Entry<IBlockState, GrassInfo> entry : grassInfoMap.entrySet()) {
            ResourceLocation baseTextureLocation = new ResourceLocation(entry.getValue().baseTextureName);
            TextureAtlasSprite baseGrassTexture = blockTextures.getTextureExtry(baseTextureLocation.toString());
            Color4 averageColor = ResourceUtils.calculateTextureColor(baseGrassTexture);
            entry.getValue().averageColor = averageColor.withHSVBrightness(1.0f);
            entry.getValue().useTextureColor = averageColor.getSaturation() > Config.grassSaturationThreshold;
        }
    }
    
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        if (event.map != blockTextures) return;
        blockTextures = null;
    }
}
