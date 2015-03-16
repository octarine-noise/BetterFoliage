package mods.betterfoliage.client.texture.generator;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class ShortGrassGenerator extends BlockTextureGenerator {

	protected boolean isSnowed = false;
	
	protected int snowOriginalWeight = 2;
	protected int snowWhiteWeight = 3;
	
	public ShortGrassGenerator(String domainName, ResourceLocation missingResource, boolean isSnowed) {
		super(domainName, missingResource);
		this.isSnowed = isSnowed;
	}

	@Override
	public IResource getResource(ResourceLocation resourceLocation) throws IOException {
		IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
		ResourceLocation originalNoDirs = unwrapResource(resourceLocation);
		ResourceLocation originalWithDirs = new ResourceLocation(originalNoDirs.getResourceDomain(), "textures/" + originalNoDirs.getResourcePath());
		
		// load full texture
		BufferedImage origImage = ImageIO.read(resourceManager.getResource(originalWithDirs).getInputStream());

		// draw bottom half of texture
		BufferedImage result = new BufferedImage(origImage.getWidth(), origImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = result.createGraphics();
		graphics.drawImage(origImage, 0, 3 * origImage.getHeight() / 8, null);

		// blend with white if snowed
		if (isSnowed && !ShadersModIntegration.isSpecialTexture(originalWithDirs)) {
			for (int x = 0; x < result.getWidth(); x++) for (int y = 0; y < result.getHeight(); y++) {
				result.setRGB(x, y, blendRGB(result.getRGB(x, y), 0xFFFFFF, 2, 3));
			}
		}
		
		return new BufferedImageResource(resourceLocation, originalWithDirs, result);
	}

}
