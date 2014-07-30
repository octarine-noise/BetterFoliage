package mods.betterfoliage.client.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.IMetadataSection;

public class BufferedImageResource implements IResource {

	/** Raw PNG data*/
	protected byte[] data = null;
	
	public BufferedImageResource(BufferedImage image) {
		// create PNG image
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "PNG", baos);
			data = baos.toByteArray();
		} catch (IOException e) {
		}
	}
	
	@Override
	public InputStream getInputStream() {
		return data == null ? null : new ByteArrayInputStream(data);
	}

	@Override
	public boolean hasMetadata() {
		return false;
	}

	@Override
	public IMetadataSection getMetadata(String var1) {
		return null;
	}

}
