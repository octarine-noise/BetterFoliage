package mods.betterfoliage.client.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

/** {@link IResource} for a {@link BufferedImage}
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class BufferedImageResource implements IResource {

	/** Raw PNG data*/
	protected byte[] data = null;
	
	/** location **/
	protected ResourceLocation resourceLocation;
	
	public BufferedImageResource(ResourceLocation resourceLocation, BufferedImage image) {
	    this.resourceLocation = resourceLocation;
	    
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

    @Override
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    @Override
    public String getResourcePackName() {
        return resourceLocation.toString();
    }

}
