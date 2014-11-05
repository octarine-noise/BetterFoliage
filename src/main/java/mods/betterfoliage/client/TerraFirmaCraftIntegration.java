package mods.betterfoliage.client;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Helper methods for dealing with TerraFirmaCraft.
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class TerraFirmaCraftIntegration {

    public static boolean isTFCLoaded = false;
    
    public static BlockMatcher blocksTFC;
    
    /** Hide constructor */
    private TerraFirmaCraftIntegration() {}
    
    public static void init() {
        if (Loader.isModLoaded("terrafirmacraft")) {
            BetterFoliage.log.info("TerraFirmaCraft found - setting up compatibility");
            isTFCLoaded = true;
            blocksTFC = new BlockMatcher() {
                @Override
                public boolean matchesClass(Block block) {
                    return Config.grass.matchesClass(block) && block.getClass().getName().startsWith("com.bioxx.tfc");
                }
            };
            MinecraftForge.EVENT_BUS.register(blocksTFC);
        }
    }
    
    public static boolean isTFCGrass(Block block) {
        return isTFCLoaded && blocksTFC.matchesID(block);
    }
}
