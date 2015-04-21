package mods.betterfoliage.client.render.impl;

import java.awt.Color;
import java.util.Collection;

import com.google.common.collect.Sets;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.texture.LeafTextures.LeafInfo;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityFXFallingLeaves extends EntityFX {

	/** {@link IBlockState}s which have had problems. List is kept to avoid massive log spam. */
	public static Collection<IBlockState> erroredStates = Sets.newHashSet();
	
    /** Quick <b>cos</b> lookup for <b>particleRotation</b> */
	protected static double[] cos = new double[64];
	
	/** Quick <b>sin</b> lookup for <b>particleRotation</b> */
	protected static double[] sin = new double[64];
	
	static {
		for (int idx = 0; idx < 64; idx++) {
			cos[idx] = Math.cos(2.0 * Math.PI / 64.0 * idx);
			sin[idx] = Math.sin(2.0 * Math.PI / 64.0 * idx);
		}
	}
	
	/** Chance each tick that the particle will change its direction of rotation */
	public static float rotationFlipChance = 0.05f;
	
	/** Flat brightness multiplier for particles (empirically determined magic number) */
	public static float biomeBrightnessMultiplier = 0.5f;
	
	/** Value of <b>isOnGround</b> for the last tick */
	public boolean wasOnGround = false;
	
	/** Mirror texture */
	public boolean isMirrored;
	
	/** Rotation of particle around screen normal axis. Integer value between [0,64). */
	public int particleRotation = 0;
	
	/** Particle is currently rotating in CCW direction */
	public boolean rotationPositive = true;
	
	public EntityFXFallingLeaves(World world, IBlockState blockState, BlockPos pos) {
		super(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		particleMaxAge = MathHelper.floor_double((0.6 + 0.4 * rand.nextDouble()) * Config.leafFXLifetime * 20.0);
		isMirrored = (rand.nextInt() & 1) == 1;
		motionY = -Config.leafFXSpeed;
		particleRotation = rand.nextInt(64);
		particleScale = (float) Config.leafFXSize;
		
		LeafInfo leafInfo = BetterFoliageClient.leafRegistry.leafInfoMap.get(blockState);
		if (leafInfo != null && leafInfo.particleType != null) {
			particleIcon = BetterFoliageClient.leafRegistry.particleTextures.get(leafInfo.particleType).get(rand.nextInt(1024));
			Color4 blockColor = Color4.fromARGB(blockState.getBlock().colorMultiplier(world, pos, 0)).opaque();
			calculateParticleColor(leafInfo.averageColor, blockColor);
		} else if (!erroredStates.contains(blockState)) {
			erroredStates.add(blockState);
			BetterFoliage.log.warn(String.format("Error creating leaf particle - unknown texture for state: %s", blockState.toString()));
		}
	}

	/** Add the particle to the supplied {@link EffectRenderer}.
	 * Operation deferred to the particle itself for easier error handling.
	 * @param renderer
	 */
	public void addToRenderer(EffectRenderer renderer) {
		if (particleIcon != null) renderer.addEffect(this);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		particleScale = (float) Config.leafFXSize;
		if (rand.nextFloat() < rotationFlipChance) rotationPositive = !rotationPositive;
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
    public void renderParticle(WorldRenderer renderer, Entity entity, float partialTickTime, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
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
        
        renderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
        
        addVertex(renderer, center.sub(vec1Rot), maxU, maxV);
        addVertex(renderer, center.sub(vec2Rot), maxU, minV);
        addVertex(renderer, center.add(vec1Rot), minU, minV);
        addVertex(renderer, center.add(vec2Rot), minU, maxV);
    }
    
	protected void addVertex(WorldRenderer renderer, Double3 coord, double u, double v) {
	    renderer.addVertexWithUV(coord.x, coord.y, coord.z, u, v);
	}
	
	/** Calculates and sets the color of the particle by blending the average color of the block texture with the current biome color.
	 *  Blending is done in HSB color space, weighted by the relative saturation of the colors.
	 * @param textureAvgColor average color of the block texture
	 * @param blockColor biome color at the spawning block
	 */
	public void calculateParticleColor(Color4 textureAvgColor, Color4 blockColor) {
		float[] hsbTexture = Color.RGBtoHSB(textureAvgColor.R, textureAvgColor.G, textureAvgColor.B, null);
		float[] hsbBlock = Color.RGBtoHSB(blockColor.R, blockColor.G, blockColor.B, null);
		
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
