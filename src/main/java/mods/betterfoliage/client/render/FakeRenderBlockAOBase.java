package mods.betterfoliage.client.render;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

/** Same as {@link RenderBlockAOBase}, but does not actually render anything.
 * @author octarine-noise
 */
public class FakeRenderBlockAOBase extends RenderBlockAOBase {

	@Override
	public void renderFaceZNeg(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoZNXYPP);
		saveShadingTopRight(aoZNXYNP);
		saveShadingBottomLeft(aoZNXYPN);
		saveShadingBottomRight(aoZNXYNN);
	}

	@Override
	public void renderFaceZPos(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoZPXYNP);
		saveShadingTopRight(aoZPXYPP);
		saveShadingBottomLeft(aoZPXYNN);
		saveShadingBottomRight(aoZPXYPN);
	}

	@Override
	public void renderFaceXNeg(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoXNYZPN);
		saveShadingTopRight(aoXNYZPP);
		saveShadingBottomLeft(aoXNYZNN);
		saveShadingBottomRight(aoXNYZNP);
	}

	@Override
	public void renderFaceXPos(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoXPYZPP);
		saveShadingTopRight(aoXPYZPN);
		saveShadingBottomLeft(aoXPYZNP);
		saveShadingBottomRight(aoXPYZNN);
	}

	@Override
	public void renderFaceYNeg(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoYNXZNP);
		saveShadingTopRight(aoYNXZPP);
		saveShadingBottomLeft(aoYNXZNN);
		saveShadingBottomRight(aoYNXZPN);
	}

	@Override
	public void renderFaceYPos(Block block, double x, double y, double z, IIcon icon) {
		saveShadingTopLeft(aoYPXZPP);
		saveShadingTopRight(aoYPXZNP);
		saveShadingBottomLeft(aoYPXZPN);
		saveShadingBottomRight(aoYPXZNN);
	}
	
}
