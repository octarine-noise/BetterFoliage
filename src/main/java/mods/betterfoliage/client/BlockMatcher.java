package mods.betterfoliage.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BlockMatcher {

	public Set<String> defaultClasses = Sets.newHashSet();
	public Set<Class<?>> classes = Sets.newHashSet();
	public Set<Integer> blockIDs = Sets.newHashSet();
	
	public BlockMatcher(String... defaults) {
		Collections.addAll(defaultClasses, defaults);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void addClass(String className) {
		try {
			classes.add(Class.forName(className));
		} catch(ClassNotFoundException e) {
		}
	}
	
	public boolean matchesClass(Block block) {
		for (Class<?> clazz : classes) if (clazz.isAssignableFrom(block.getClass())) return true;
		return false;
	}
	
	public boolean matchesID(int blockId) {
		return blockIDs.contains(blockId);
	}
	
	public boolean matchesID(Block block) {
		return blockIDs.contains(Block.blockRegistry.getIDForObject(block));
	}
	
	public void load(File file) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line != null) {
				addClass(line.trim());
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			saveDefaults(file);
			for (String clazz : defaultClasses) addClass(clazz);
		} catch (IOException e) {
			BetterFoliage.log.warn(String.format("Error reading configuration: %s", file.getName()));
		}
	}
	
	public void saveDefaults(File file) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			for (String clazz : defaultClasses) {
				writer.write(clazz);
				writer.write("\n");
			}
			writer.close();
		} catch (FileNotFoundException e) {
			saveDefaults(file);
		} catch (IOException e) {
			BetterFoliage.log.warn(String.format("Error writing default configuration: %s", file.getName()));
		}
	}
	
	/** Caches block IDs on world load for fast lookup
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		Iterator<Block> iter = Block.blockRegistry.iterator();
		while (iter.hasNext()) {
			Block block = iter.next();
			if (matchesClass(block)) blockIDs.add(Block.blockRegistry.getIDForObject(block));
		}
	}
}
