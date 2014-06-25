package mods.betterfoliage;

import net.minecraft.block.Block;

/** Allows overriding block rendertype.
 * @author octarine-noise
 */
public class BlockRenderTypeOverride {

	public static IRenderTypeProvider provider = null;
	
	public static interface IRenderTypeProvider {
		public int getRenderType(Block block);
	}
	
	/** Entry point from transformed RenderBlocks class. If no provider is given,
	 *  replicates default behaviour
	 * @param block block instance
	 * @return block render type
	 */
	public static int getRenderType(Block block) {
		return provider == null ? block.getRenderType() : provider.getRenderType(block);
	}
}