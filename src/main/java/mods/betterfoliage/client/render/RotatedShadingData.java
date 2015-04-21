package mods.betterfoliage.client.render;

import net.minecraft.util.EnumFacing;

/** Wrapper for {@link IShadingData} to handle {@link Rotation}s
 * @author octarine-noise
 */
public class RotatedShadingData implements IShadingData {

	/** Underlying {@link IShadingData} that corresponds to the identity rotation */
	protected IShadingData wrapped;
	
	/** {@link Rotation} applied to the wrapped shading data */
	protected Rotation rotation;
	
	public RotatedShadingData(IShadingData wrapped, Rotation rotation) {
		this.wrapped = wrapped;
		this.rotation = rotation;
	}

	@Override
	public int getBrightness(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax) {
		return wrapped.getBrightness(rotation.transform(primary), rotation.transform(secondary), rotation.transform(tertiary), useMax);
	}

	@Override
	public float getColorMultiplier(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax) {
		return wrapped.getColorMultiplier(rotation.transform(primary), rotation.transform(secondary), rotation.transform(tertiary), useMax);
	}

	@Override
	public boolean shouldUseAO() {
		return wrapped.shouldUseAO();
	}

	public static <T extends IShadingData> ThreadLocal<IShadingData> wrap(final ThreadLocal<T> wrapped, final Rotation rotation) {
		return new ThreadLocal<IShadingData>() {
			@Override
			protected RotatedShadingData initialValue() {
				return new RotatedShadingData(wrapped.get(), rotation);
			}
		};
	}

}
