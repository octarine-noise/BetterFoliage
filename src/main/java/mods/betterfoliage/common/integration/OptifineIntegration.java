package mods.betterfoliage.common.integration;

import java.util.Collection;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.loader.IResolvable;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.google.common.collect.Sets;

public class OptifineIntegration {

	public static boolean isPresent = false;
	
	public static void init() {
		boolean some = false, all = true;
		for (IResolvable<?> element : CodeRefs.optifineCTF) {
			if (element.resolve() == null) {
				all = false;
			} else {
				some = true;
			}
		}
		if (all) {
			BetterFoliage.log.info("Found Optifine, using CTM support");
			isPresent = true;
		} else if (some) {
			BetterFoliage.log.info("Found Optifine, but not all needed elements (wrong version?)");
		}
	}
	
	public static Collection<IIcon> getAllCTMForBlock(Block block) {
		Collection<IIcon> result = Sets.newHashSet();
		if (!isPresent) return result;
		
		Object[][] blockProperties = CodeRefs.fCTBlockProperties.getStaticField();
		int blockId = Block.getIdFromBlock(block);
		
		if (blockProperties != null && blockId < blockProperties.length && blockId >= 0) {
			Object[] connectedProperties = blockProperties[blockId];
			if (connectedProperties != null) {
				for (Object cp : connectedProperties) {
					IIcon[] icons = CodeRefs.fCPTileIcons.getInstanceField(cp);
					for (int idx = 0; idx < icons.length; idx++) result.add(icons[idx]);
				}
			}
		}
		return result;
	}
	
	public static Collection<IIcon> getAllCTMForIcon(IIcon icon) {
		Collection<IIcon> result = Sets.newHashSet();
		if (!isPresent) return result;
		
		Object[][] tileProperties = CodeRefs.fCTTileProperties.getStaticField();
		int iconIdx = CodeRefs.mGetIndexInMap.invokeInstanceMethod(icon);
		
		if (tileProperties != null && iconIdx < tileProperties.length && iconIdx >= 0) {
			Object[] connectedProperties = tileProperties[iconIdx];
			if (connectedProperties != null) {
				for (Object cp : connectedProperties) {
					IIcon[] icons = CodeRefs.fCPTileIcons.getInstanceField(cp);
					for (int idx = 0; idx < icons.length; idx++) result.add(icons[idx]);
				}
			}
		}
		return result;
	}
	
	public static IIcon getConnectedTexture(IBlockAccess blockAccess, Block block, int x, int y, int z, int side, IIcon icon) {
		return CodeRefs.mGetConnectedTexture.invokeStaticMethod(blockAccess, block, x, y, z, side, icon);
	}
}
