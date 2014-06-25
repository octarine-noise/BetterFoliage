package mods.betterfoliage.client.resource;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@SideOnly(Side.CLIENT)
public interface ILeafTextureRecognizer {

	public boolean isLeafTexture(TextureAtlasSprite icon);
}
