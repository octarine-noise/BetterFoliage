package mods.betterfoliage.client.texture.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** {@link IResource} for a {@link BufferedImage}
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class BufferedImageResource implements IResource {

	/** Raw PNG data */
	protected byte[] data = null;
	
	/** location **/
	protected ResourceLocation resourceLocation;
	
	/** Underlying resource for generated resources, get metadata from this */
    ResourceLocation metaSource;
    
	public BufferedImageResource(ResourceLocation resourceLocation, ResourceLocation metaSource, BufferedImage image) {
	    this.resourceLocation = resourceLocation;
	    this.metaSource = metaSource;
	    
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
	    try {
            return metaSource == null ? false : Minecraft.getMinecraft().getResourceManager().getResource(metaSource).hasMetadata();
        } catch (IOException e) {
            return false;
        }
	}

	@Override
	public IMetadataSection getMetadata(String type) {
        try {
            return metaSource == null ? null : Minecraft.getMinecraft().getResourceManager().getResource(metaSource).getMetadata(type);
        } catch (IOException e) {
            return null;
        }
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
