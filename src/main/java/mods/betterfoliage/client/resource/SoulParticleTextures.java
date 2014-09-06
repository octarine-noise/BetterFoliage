package mods.betterfoliage.client.resource;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.IconSet;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Holds the textures for the rising soul particles
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class SoulParticleTextures {

    public IIcon soulTrackIcon;
    
    public IconSet soulHeadIcons = new IconSet("bettergrassandleaves", "rising_soul_%d");
    
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 0) return;
        
        soulTrackIcon = event.map.registerIcon("bettergrassandleaves:soul_track");
        soulHeadIcons.registerIcons(event.map);
    }
    
    @SubscribeEvent
    public void endTextureReload(TextureStitchEvent.Post event) {
        if (event.map.getTextureType() == 0) BetterFoliage.log.info(String.format("Found %d soul particle textures", soulHeadIcons.numLoaded));
    }
}
