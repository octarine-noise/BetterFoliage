package mods.betterfoliage.client.misc;

import java.util.Random;

import mods.betterfoliage.common.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
		double speed = Math.abs(random.nextGaussian()) * Config.leafFXWindStrength;
		if (world.isRaining()) speed += Math.abs(random.nextGaussian()) * Config.leafFXStormStrength;
		
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
