package mods.betterfoliage.client.texture.models;

import com.google.common.base.Function;

import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/** {@link Function} to extract base leaf texture name from an {@link IModel}
 * @author octarine-noise
 *
 */
@SideOnly(Side.CLIENT)
public interface IModelTextureMapping extends Function<IModel, String> {
}
