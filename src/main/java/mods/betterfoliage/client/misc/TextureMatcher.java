package mods.betterfoliage.client.misc;

import java.util.List;

import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class TextureMatcher {
    
    public static final String DEFAULT_NAME = "default";
    
    public static class TextureMapping {
        public String matchDomain;
        public String matchName;
        public String textureType;
        
        public TextureMapping(String matchDomain, String matchName, String textureType) {
            this.matchDomain = matchDomain;
            this.matchName = matchName;
            this.textureType = textureType;
        }
        
        public boolean matches(TextureAtlasSprite icon) {
            ResourceLocation iconLocation = new ResourceLocation(icon.getIconName());
            if (matchDomain != null && !matchDomain.equals(iconLocation.getResourceDomain())) return false;
            return iconLocation.getResourcePath().contains(matchName);
        }
    }
    
    public List<TextureMapping> mappings = Lists.newLinkedList();
    
    public String get(TextureAtlasSprite icon) {
        for (TextureMapping mapping : mappings) if (mapping.matches(icon)) return mapping.textureType;
        return DEFAULT_NAME;
    }
    
    public void loadMappings(ResourceLocation resource) {
        mappings = Lists.newLinkedList();
        
        for (String line : ResourceUtils.getLines(resource)) {
            String[] lineSplit = line.split("=");
            if (lineSplit.length != 2) continue;
            
            String[] match = lineSplit[0].split(":");
            if (match.length == 2) {
                mappings.add(new TextureMapping(match[0], match[1], lineSplit[1]));
            } else if (match.length == 1) {
                mappings.add(new TextureMapping(null, match[0], lineSplit[1]));
            }
        }
    }
    
}
