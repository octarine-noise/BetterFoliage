package mods.betterfoliage.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockMatcher {

	public Set<Class<?>> whiteList = Sets.newHashSet();
	public Set<Class<?>> blackList = Sets.newHashSet();
	public Set<Integer> blockIDs = Sets.newHashSet();
	
	public void addClass(String className) {
		try {
			if (className.startsWith("-"))
				blackList.add(Class.forName(className.substring(1)));
			else
				whiteList.add(Class.forName(className));
		} catch(ClassNotFoundException e) {}
	}
	
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
	
	public void load(File file, ResourceLocation defaults) {
		if (!file.exists()) Utils.copyFromTextResource(defaults, file);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			whiteList.clear();
			blackList.clear();
			String line = reader.readLine();
			while(line != null) {
				addClass(line.trim());
				line = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			BetterFoliage.log.warn(String.format("Error reading configuration: %s", file.getName()));
		}
	}
	
	/** Caches block IDs on world load for fast lookup
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		blockIDs.clear();
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while (iter.hasNext()) {
			Block block = iter.next();
			if (matchesClass(block)) blockIDs.add(Block.blockRegistry.getIDForObject(block));
		}
	}
}
