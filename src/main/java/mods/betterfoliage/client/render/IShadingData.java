package mods.betterfoliage.client.render;

import net.minecraft.util.EnumFacing;

public interface IShadingData {

	/** Get the brightness value to use for a vertex in a given block corner. The corner is defined by its 3 worldspace directions (eg. TOP-SOUTH-WEST).
	 * Since there are 3 faces meeting in a corner, the primary direction determines which value to use.
	 * @param primary direction of face
	 * @param secondary direction of corner on face
	 * @param tertiary direction of corner on face
	 * @param useMax if true, check the other 2 permutations and return the maximum value
	 * @return brightness of block corner
	 */
	public abstract int getBrightness(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax);

	/** Get the color multiplier to use for a vertex in a given block corner. The corner is defined by its 3 worldspace directions (eg. TOP-SOUTH-WEST).
	 * Since there are 3 faces meeting in a corner, the primary direction determines which value to use.
	 * @param primary direction of face
	 * @param secondary direction of corner on face
	 * @param tertiary direction of corner on face
	 * @param useMax if true, check the other 2 permutations and return the maximum value
	 * @return color multiplier of block corner
	 */
	public abstract float getColorMultiplier(EnumFacing primary, EnumFacing secondary, EnumFacing tertiary, boolean useMax);

	public boolean shouldUseAO();
}