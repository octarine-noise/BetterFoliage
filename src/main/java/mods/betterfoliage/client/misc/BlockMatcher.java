package mods.betterfoliage.client.misc;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import mods.betterfoliage.client.util.ResourceUtils;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Sets;

@SideOnly(Side.CLIENT)
public class BlockMatcher {

	public Set<Class<?>> whiteList = Sets.newHashSet();
	public Set<Class<?>> blackList = Sets.newHashSet();
	public Set<Integer> blockIDs = Sets.newHashSet();

	public boolean matchesClass(Block block) {
		for (Class<?> clazz : blackList) if (clazz.isAssignableFrom(block.getClass())) return false;
		for (Class<?> clazz : whiteList) if (clazz.isAssignableFrom(block.getClass())) return true;
		return false;
	}
	
	public boolean matchesID(int blockId) {
		return blockIDs.contains(blockId);
	}
	
	public boolean matchesID(Block block) {
		return blockIDs.contains(Block.blockRegistry.getIDForObject(block));
	}
	
	public void updateClassLists(String[] newWhitelist, String[] newBlacklist) {
	    whiteList.clear();
	    for(String className : newWhitelist) try {
	        whiteList.add(Class.forName(className));
	    } catch(ClassNotFoundException e) {}
	    
	    blackList.clear();
        for(String className : newBlacklist) try {
            blackList.add(Class.forName(className));
        } catch(ClassNotFoundException e) {}
        
        updateBlockIDs();
	}
	
	@SuppressWarnings("unchecked")
    public void updateBlockIDs() {
	    blockIDs.clear();
        Iterator<Block> iter = Block.blockRegistry.iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            if (matchesClass(block)) blockIDs.add(Block.blockRegistry.getIDForObject(block));
        }
	}
	
	public static void loadDefaultLists(ResourceLocation defaults, Collection<String> blacklist, Collection<String> whitelist) {
	    for (String line : ResourceUtils.getLines(defaults)) {
	        if (line.startsWith("-"))
                blacklist.add(line.substring(1));
            else
                whitelist.add(line);
	    }
	}
	
	/** Caches block IDs on world load for fast lookup
	 * @param event
	 */
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		updateBlockIDs();
	}
}
