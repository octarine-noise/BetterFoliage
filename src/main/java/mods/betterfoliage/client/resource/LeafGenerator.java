package mods.betterfoliage.client.resource;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.ShadersModIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LeafGenerator extends LeafGeneratorBase {

	/** Name of the default alpha mask to use */
	public static String defaultMask = "default";
	
	public LeafGenerator() {
		super("bf_leaves", "betterfoliage", "textures/blocks/%s/%s", "betterfoliage:textures/blocks/leafmask_%d_%s.png", BetterFoliageClient.missingTexture);
	}

	@Override
	protected BufferedImage generateLeaf(ResourceLocation originalWithDirs) throws IOException, TextureGenerationException {
		// load normal leaf texture
		BufferedImage origImage = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(originalWithDirs).getInputStream());
		if (origImage.getWidth() != origImage.getHeight()) throw new TextureGenerationException();
		int size = origImage.getWidth();

		// tile leaf texture 2x2
		BufferedImage overlayIcon = new BufferedImage(size * 2, size * 2, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = overlayIcon.createGraphics();
		graphics.drawImage(origImage, 0, 0, null);
		graphics.drawImage(origImage, 0, size, null);
		graphics.drawImage(origImage, size, 0, null);
		graphics.drawImage(origImage, size, size, null);
		
		// overlay mask alpha on texture
		if (!ShadersModIntegration.isSpecialTexture(originalWithDirs)) {
			// load alpha mask of appropriate size
			BufferedImage maskImage = loadLeafMaskImage(defaultMask, size * 2);
			int scale = size * 2 / maskImage.getWidth();
			
			for (int x = 0; x < overlayIcon.getWidth(); x++) for (int y = 0; y < overlayIcon.getHeight(); y++) {
				long origPixel = overlayIcon.getRGB(x, y) & 0xFFFFFFFFl;
				long maskPixel = maskImage.getRGB(x / scale, y / scale) & 0xFF000000l | 0x00FFFFFF;
				overlayIcon.setRGB(x, y, (int) (origPixel & maskPixel));
			}
		}
		
		return overlayIcon;
	}
	
	@Override
	@SubscribeEvent
	public void endTextureReload(Post event) {
		super.endTextureReload(event);
		if (event.map.getTextureType() != 0) return;
		BetterFoliage.log.info(String.format("Found %d pre-drawn leaf textures", drawnCounter));
		BetterFoliage.log.info(String.format("Generated %d leaf textures", generatedCounter));
	}
}
