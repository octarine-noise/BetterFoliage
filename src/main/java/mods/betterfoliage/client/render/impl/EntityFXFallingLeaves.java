package mods.betterfoliage.client.render.impl;

import java.awt.Color;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.common.util.Double3;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFXFallingLeaves extends EntityFX {

	protected static double[] cos = new double[64];
	protected static double[] sin = new double[64];
	
	static {
		for (int idx = 0; idx < 64; idx++) {
			cos[idx] = Math.cos(2.0 * Math.PI / 64.0 * idx);
			sin[idx] = Math.sin(2.0 * Math.PI / 64.0 * idx);
		}
	}
	
	public static float biomeBrightnessMultiplier = 0.5f;
	public boolean wasOnGround = false;
	public boolean isMirrored;
	public int particleRotation = 0;
	public boolean rotationPositive = true;
	
	public EntityFXFallingLeaves(World world, int x, int y, int z) {
		super(world, x + 0.5, y, z + 0.5);
		particleMaxAge = MathHelper.floor_double((0.6 + 0.4 * rand.nextDouble()) * Config.leafFXLifetime * 20.0);
		isMirrored = (rand.nextInt() & 1) == 1;
		motionY = -Config.leafFXSpeed;
		particleRotation = rand.nextInt(64);
		particleScale = (float) Config.leafFXSize;
		
		
		Block block = world.getBlock(x, y, z);
		IIcon blockIcon = block.getIcon(world, x, y, z, ForgeDirection.DOWN.ordinal());
		particleIcon = BetterFoliageClient.leafParticles.getIconSet(blockIcon).get(rand.nextInt(1024));
		calculateParticleColor(BetterFoliageClient.leafParticles.getColor(blockIcon), block.colorMultiplier(world, x, y, z));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		particleScale = (float) Config.leafFXSize;
		if (rand.nextFloat() > 0.95f) rotationPositive = !rotationPositive;
		if (particleAge > particleMaxAge - 20) particleAlpha = 0.05f * (particleMaxAge - particleAge);
		
		if (onGround || wasOnGround) {
			motionX = 0.0;
			motionZ = 0.0;
			motionZ = 0.0;
			if (!wasOnGround) {
				particleAge = Math.max(particleAge, particleMaxAge - 20);
			}
			wasOnGround = true;
		} else {
			motionX = (BetterFoliageClient.wind.currentX + cos[particleRotation] * Config.leafFXPerturb) * Config.leafFXSpeed;
			motionZ = (BetterFoliageClient.wind.currentZ + sin[particleRotation] * Config.leafFXPerturb) * Config.leafFXSpeed;
			motionY = -Config.leafFXSpeed;
			particleRotation = (particleRotation + (rotationPositive ? 1 : -1)) & 63;
		}
	}

	@Override
    public void renderParticle(Tessellator tessellator, float partialTickTime, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ)
    {
        float minU = isMirrored ? particleIcon.getMinU() : particleIcon.getMaxU();
        float maxU = isMirrored ? particleIcon.getMaxU() : particleIcon.getMinU();
        float minV = particleIcon.getMinV();
        float maxV = particleIcon.getMaxV();
        float scale = 0.1F * this.particleScale;
        
        Double3 center = new Double3(prevPosX + (posX - prevPosX) * partialTickTime - interpPosX, 
        							 prevPosY + (posY - prevPosY) * partialTickTime - interpPosY,
        							 prevPosZ + (posZ - prevPosZ) * partialTickTime - interpPosZ);
        Double3 vec1 = new Double3(rotX + rotXY, rotZ, rotYZ + rotXZ).scale(scale);
        Double3 vec2 = new Double3(rotX - rotXY, -rotZ, rotYZ - rotXZ).scale(scale);
        Double3 vec1Rot = vec1.scale(cos[particleRotation]).add(vec2.scale(sin[particleRotation]));
        Double3 vec2Rot = vec1.scale(-sin[particleRotation]).add(vec2.scale(cos[particleRotation]));
        
        tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        addVertex(tessellator, center.sub(vec1Rot), maxU, maxV);
        addVertex(tessellator, center.sub(vec2Rot), maxU, minV);
        addVertex(tessellator, center.add(vec1Rot), minU, minV);
        addVertex(tessellator, center.add(vec2Rot), minU, maxV);
    }
    
	protected void addVertex(Tessellator tessellator, Double3 coord, double u, double v) {
		tessellator.addVertexWithUV(coord.x, coord.y, coord.z, u, v);
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
