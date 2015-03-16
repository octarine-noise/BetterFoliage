package mods.betterfoliage.client.render.impl;

import java.util.Deque;
import java.util.Iterator;

import mods.betterfoliage.client.BetterFoliageClient;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.common.config.Config;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

@SideOnly(Side.CLIENT)
public class EntityFXRisingSoul extends EntityFX {

    protected static double[] cos = new double[64];
    protected static double[] sin = new double[64];
    
    static {
        for (int idx = 0; idx < 64; idx++) {
            cos[idx] = Math.cos(2.0 * Math.PI / 64.0 * idx);
            sin[idx] = Math.sin(2.0 * Math.PI / 64.0 * idx);
        }
    }
    
    public int initialPhase;
    
    public Deque<Double3> particleTrail = Lists.newLinkedList();
    
    public EntityFXRisingSoul(World world, BlockPos pos) {
        super(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        
        motionY = 0.1f;
        particleGravity = 0.0f;
        
        particleIcon = BetterFoliageClient.soulParticles.soulHeadIcons.get(rand.nextInt(256));
        particleMaxAge = MathHelper.floor_double((0.6 + 0.4 * rand.nextDouble()) * Config.soulFXLifetime * 20.0);
        initialPhase = rand.nextInt(64);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        
        int phase = (initialPhase + particleAge) % 64;
        
        motionY = 0.1f;
        motionX = cos[phase] * Config.soulFXPerturb;
        motionZ = sin[phase] * Config.soulFXPerturb;
        
        particleTrail.addFirst(new Double3(posX, posY, posZ));
        while (particleTrail.size() > Config.soulFXTrailLength) particleTrail.removeLast();
    }
    
    @Override
    public void renderParticle(WorldRenderer renderer, Entity entity, float partialTickTime, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {
        Double3 vec1 = new Double3(rotX + rotXY, rotZ, rotYZ + rotXZ);
        Double3 vec2 = new Double3(rotX - rotXY, -rotZ, rotYZ - rotXZ);
        
        Iterator<Double3> iter = particleTrail.iterator();
        Double3 current, previous;
        TextureAtlasSprite renderIcon = particleIcon;
        double scale = Config.soulFXHeadSize * 0.25;
        float alpha = (float) Config.soulFXOpacity;
        if (particleAge > particleMaxAge - 40) alpha *= (particleMaxAge - particleAge) / 40.0f;
        
        int idx = 0;
        if (iter.hasNext()) {
            previous = iter.next();
            while(iter.hasNext()) {
                current = previous;
                previous = iter.next();
                
                if (idx++ % Config.soulFXTrailDensity == 0) {
                    renderer.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, alpha);
                    renderParticleQuad(renderer, partialTickTime, current, previous, vec1, vec2, renderIcon, scale);
                }
                if (idx == 1) {
                    // set initial trail particle size and icon after rendering head
                    scale = Config.soulFXTrailSize * 0.25;
                    renderIcon = BetterFoliageClient.soulParticles.soulTrackIcon;
                }
                scale *= Config.soulFXSizeDecay;
                alpha *= Config.soulFXOpacityDecay;
            }
        }
    }
    
    protected void renderParticleQuad(WorldRenderer renderer, float partialTickTime, Double3 currentPos, Double3 previousPos, Double3 vec1, Double3 vec2, TextureAtlasSprite texture, double scale) {
        float minU = texture.getMinU();
        float maxU = texture.getMaxU();
        float minV = texture.getMinV();
        float maxV = texture.getMaxV();
        
        Double3 center = new Double3(previousPos.x + (currentPos.x - previousPos.x) * partialTickTime - interpPosX, 
                                     previousPos.y + (currentPos.y - previousPos.y) * partialTickTime - interpPosY,
                                     previousPos.z + (currentPos.z - previousPos.z) * partialTickTime - interpPosZ);
        
        addVertex(renderer, center.sub(vec1.scale(scale)), maxU, maxV);
        addVertex(renderer, center.sub(vec2.scale(scale)), maxU, minV);
        addVertex(renderer, center.add(vec1.scale(scale)), minU, minV);
        addVertex(renderer, center.add(vec2.scale(scale)), minU, maxV);
    }
    
    protected void addVertex(WorldRenderer renderer, Double3 coord, double u, double v) {
        renderer.addVertexWithUV(coord.x, coord.y, coord.z, u, v);
    }
    
    @Override
    public int getFXLayer() {
        return 1;
    }
}
