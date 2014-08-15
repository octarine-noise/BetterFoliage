package mods.betterfoliage.client;

import java.util.Random;

import mods.betterfoliage.BetterFoliage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;

public class WindTracker {

	public Random random = new Random();
	
	public double targetX;
	public double targetZ;

	public double currentX;
	public double currentZ;
	
	public long nextChange = 0;
	
	public void changeWind(World world) {
		long changeTime = 120;
		nextChange = world.getWorldInfo().getWorldTime() + changeTime;
		
		double direction = 2.0 * Math.PI * random.nextDouble();
		double speed = Math.abs(random.nextGaussian()) * BetterFoliage.config.fallingLeavesWindStrength.value;
		if (world.isRaining()) speed += Math.abs(random.nextGaussian()) * BetterFoliage.config.fallingLeavesStormStrength.value;
		
		targetX = Math.cos(direction) * speed;
		targetZ = Math.sin(direction) * speed;
	}
	
	@SubscribeEvent
	public void handleWorldTick(ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START) return;
		World world = Minecraft.getMinecraft().theWorld;
		if (world == null) return;
		
		// change target wind speed
		if (world.getWorldInfo().getWorldTime() >= nextChange) changeWind(world);
		
		// change current wind speed
		double changeRate = world.isRaining() ? 0.015 : 0.005;
		
		double deltaX = targetX - currentX;
		if (deltaX < -changeRate) deltaX = -changeRate;
		if (deltaX > changeRate) deltaX = changeRate;
		double deltaZ = targetZ - currentZ;
		if (deltaZ < -changeRate) deltaZ = -changeRate;
		if (deltaZ > changeRate) deltaZ = changeRate;
		
		currentX += deltaX;
		currentZ += deltaZ;
	}
	
	@SubscribeEvent
	public void handleWorldLoad(WorldEvent.Load event) {
		if (event.world.isRemote) changeWind(event.world);
	}
}
