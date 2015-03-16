package mods.betterfoliage.client.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.integration.ShadersModIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LeafGenerator extends LeafGeneratorBase {

	/** Name of the default alpha mask to use */
	public static String defaultMask = "default";
	
	public LeafGenerator() {
		super("bf_leaves", "betterfoliage", "%s/%s", "betterfoliage:textures/blocks/leafmask_%d_%s.png", BetterFoliageClient.missingTexture);
	}

	@Override
	protected BufferedImage generateLeaf(ResourceLocation originalResource) throws IOException, TextureGenerationException {
		// load normal leaf texture
		BufferedImage origFullIcon = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(originalResource).getInputStream());
		int size = origFullIcon.getWidth();
		int frames = origFullIcon.getHeight() / origFullIcon.getWidth();
		if (origFullIcon.getHeight() % origFullIcon.getWidth() != 0) throw new TextureGenerationException();
		
		BufferedImage genFullIcon = new BufferedImage(size * 2, size * frames * 2, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D genFullGraphics = genFullIcon.createGraphics();

        // iterate all frames
        for (int frame = 0; frame < frames; frame++) {
            BufferedImage origIcon = origFullIcon.getSubimage(0, size * frame, size, size);
            
    		// tile leaf texture 2x2
    		BufferedImage genIcon = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_4BYTE_ABGR);
    		Graphics2D genGraphics = genIcon.createGraphics();
    		genGraphics.drawImage(origIcon, 0, 0, null);
    		genGraphics.drawImage(origIcon, 0, size, null);
    		genGraphics.drawImage(origIcon, size, 0, null);
    		genGraphics.drawImage(origIcon, size, size, null);
    		
    		// overlay mask alpha on texture
    		if (!ShadersModIntegration.isSpecialTexture(originalResource)) {
    			// load alpha mask of appropriate size
    			BufferedImage maskImage = loadLeafMaskImage(defaultMask, size * 2);
    			int scale = size * 2 / maskImage.getWidth();
    			
    			for (int x = 0; x < genIcon.getWidth(); x++) for (int y = 0; y < genIcon.getHeight(); y++) {
    				long origPixel = genIcon.getRGB(x, y) & 0xFFFFFFFFl;
    				long maskPixel = maskImage.getRGB(x / scale, y / scale) & 0xFF000000l | 0x00FFFFFF;
    				genIcon.setRGB(x, y, (int) (origPixel & maskPixel));
    			}
    		}
		
    		// add to animated png
    		genFullGraphics.drawImage(genIcon, 0, size * frame * 2, null);
        }
        
        return genFullIcon;
	}
	
	@Override
	@SubscribeEvent
	public void endTextureReload(TextureStitchEvent.Post event) {
		super.endTextureReload(event);
		if (event.map.getTextureType() != 0) return;
		BetterFoliage.log.info(String.format("Loaded leaf textures: %d generated, %d pre-drawn", generatedCounter, drawnCounter));
	}
}
