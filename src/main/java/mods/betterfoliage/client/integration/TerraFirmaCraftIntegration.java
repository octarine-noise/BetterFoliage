package mods.betterfoliage.client.integration;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.misc.BlockMatcher;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Helper methods for dealing with TerraFirmaCraft.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class TerraFirmaCraftIntegration extends AbstractModIntegration {

    public static boolean isTFCLoaded = false;
    
    public static BlockMatcher blocksTFC;
    
    public static BlockMatcher blockLogHoriz;
    public static BlockMatcher blockLogVert;
    
    /** Hide constructor */
    private TerraFirmaCraftIntegration() {}
    
    public static void init() {
        if (Loader.isModLoaded("terrafirmacraft")) {
            BetterFoliage.log.info("TerraFirmaCraft found - setting up compatibility");
            isTFCLoaded = true;
            blocksTFC = new BlockMatcher() {
                @Override
                public boolean matchesClass(Block block) {
                    return (Config.grass.matchesClass(block) || Config.logs.matchesClass(block)) && block.getClass().getName().startsWith("com.bioxx.tfc");
                }
            };
            blockLogHoriz = new BlockMatcher() {
                @Override
                public boolean matchesClass(Block block) {
                    return Config.logs.matchesClass(block) && block.getClass().getName().contains("Horiz");
                }
            };
            
            MinecraftForge.EVENT_BUS.register(blocksTFC);
            MinecraftForge.EVENT_BUS.register(blockLogHoriz);
        }
    }
    
    public static boolean isTFCBlock(Block block) {
        return isTFCLoaded && blocksTFC.matchesID(block);
    }
    
    public static ForgeDirection getLogVerticalDir(IBlockAccess blockAccess, int x, int y, int z) {
    	if (blockLogHoriz.matchesID(blockAccess.getBlock(x, y, z))) {
    		return (blockAccess.getBlockMetadata(x, y, z) >> 3) == 0 ? ForgeDirection.SOUTH : ForgeDirection.EAST;
    	}
        return ForgeDirection.UP;
        
    }
}
