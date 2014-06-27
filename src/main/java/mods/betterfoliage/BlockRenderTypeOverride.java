package mods.betterfoliage;

import net.minecraft.block.Block;

/** Allows overriding block rendertype.
 * @author octarine-noise
 */
public class BlockRenderTypeOverride {

	public static IRenderTypeProvider provider = null;
	
	public static interface IRenderTypeProvider {
		public int getRenderType(int original, Block block);
	}
	
	/** Entry point from transformed RenderBlocks class. If no provider is given,
	 *  replicates default behaviour
	 * @param block block instance
	 * @return block render type
	 */
	public static int getRenderTypeOverride(int orig, Block block) {
		return provider == null ? orig : provider.getRenderType(orig, block);
	}
}