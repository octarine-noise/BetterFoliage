package mods.betterfoliage.client.render.impl;

import java.awt.Color;
import java.util.Random;

import mods.betterfoliage.client.BetterFoliageClient;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFXFallingLeaves extends EntityFX {

	public static float biomeBrightnessMultiplier = 0.5f;
	public double motionJitterX;
	public double motionJitterY;
	public boolean wasOnGround = false;
	
	public EntityFXFallingLeaves(World world, int x, int y, int z) {
		super(world, x + 0.5, y, z + 0.5);
		Random random = new Random();
		motionJitterX = Math.abs(random.nextGaussian()) * 0.005;
		motionJitterY = Math.abs(random.nextGaussian()) * 0.005;
		particleMaxAge = 40 + random.nextInt(40);
		motionY = -0.05d;
		
		particleScale = 0.75f;
		particleIcon = BetterFoliageClient.leafParticles.icon;
		
		Block block = world.getBlock(x, y, z);
		IIcon blockIcon = block.getIcon(world, x, y, z, ForgeDirection.DOWN.ordinal());
		calculateParticleColor(BetterFoliageClient.leafParticles.getColor(blockIcon), block.colorMultiplier(world, x, y, z));

	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if (onGround || wasOnGround) {
			wasOnGround = true;
			motionX = 0.0;
			motionZ = 0.0;
			motionZ = 0.0;
			if (!wasOnGround) {
				particleAge = particleMaxAge - 10;
			}
		} else {
			motionX = BetterFoliageClient.wind.currentX + motionJitterX;
			motionZ = BetterFoliageClient.wind.currentZ + motionJitterY;
			motionY = -0.05d;
		}
	}

	@Override
	public void renderParticle(Tessellator tessellator, float partialTickTime, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		super.renderParticle(tessellator, partialTickTime, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	/** Calculates and sets the color of the particle by blending the average color of the block texture with the current biome color
	 *  Blending is done in HSB color space, weighted by the relative saturation of the colors
	 * @param textureAvgColor average color of the block texture
	 * @param blockColor biome color at the spawning block
	 */
	public void calculateParticleColor(int textureAvgColor, int blockColor) {
		float[] hsbTexture = Color.RGBtoHSB((textureAvgColor >> 16) & 0xFF, (textureAvgColor >> 8) & 0xFF, textureAvgColor & 0xFF, null);
		float[] hsbBlock = Color.RGBtoHSB((blockColor >> 16) & 0xFF, (blockColor >> 8) & 0xFF, blockColor & 0xFF, null);
		
		float weightTex = hsbTexture[1] / (hsbTexture[1] + hsbBlock[1]);
		float weightBlock = 1.0f - weightTex;
		
		// avoid circular average for hue for performance reasons
		// one of the color components should dominate anyway
		float h = weightTex * hsbTexture[0] + weightBlock * hsbBlock[0];
		float s = weightTex * hsbTexture[1] + weightBlock * hsbBlock[1];
		float b = weightTex * hsbTexture[2] + weightBlock * hsbBlock[2] * biomeBrightnessMultiplier;
		int particleColor = Color.HSBtoRGB(h, s, b);
		
		particleBlue = (particleColor & 0xFF) / 256.0f;
		particleGreen = ((particleColor >> 8) & 0xFF) / 256.0f;
		particleRed = ((particleColor >> 16) & 0xFF) / 256.0f;
	}
	
	@Override
	public int getFXLayer() {
		return 1;
	}

}
