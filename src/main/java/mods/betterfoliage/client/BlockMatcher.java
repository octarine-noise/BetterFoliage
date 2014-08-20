package mods.betterfoliage.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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
	    BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(defaults).getInputStream(), Charsets.UTF_8));
            String line = reader.readLine();
            while(line != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("//")) {
                    if (line.startsWith("-"))
                        blacklist.add(line.substring(1));
                    else
                        whitelist.add(line);
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            BetterFoliage.log.warn(String.format("Error reading configuration %s", defaults.toString()));
        }
	}
	
	/** Caches block IDs on world load for fast lookup
	 * @param event
	 */
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		if (event.world instanceof WorldClient) updateBlockIDs();
	}
}
