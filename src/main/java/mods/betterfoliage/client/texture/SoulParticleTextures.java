package mods.betterfoliage.client.texture;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.TextureSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Holds the textures for the rising soul particles
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class SoulParticleTextures {

    public TextureAtlasSprite soulTrackIcon;
    
    public TextureSet soulHeadIcons = new TextureSet("bettergrassandleaves", "blocks/rising_soul_%d");
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        soulTrackIcon = event.map.registerSprite(new ResourceLocation("bettergrassandleaves:blocks/soul_track"));
        soulHeadIcons.registerSprites(event.map);
    }
    
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        BetterFoliage.log.info(String.format("Found %d soul particle textures", soulHeadIcons.numLoaded));
    }
}
