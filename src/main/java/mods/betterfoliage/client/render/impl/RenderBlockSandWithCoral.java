package mods.betterfoliage.client.render.impl;

import java.util.Random;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.misc.Double3;
import mods.betterfoliage.client.render.BlockShadingData;
import mods.betterfoliage.client.render.TextureSet;
import mods.betterfoliage.client.render.impl.primitives.Color4;
import mods.betterfoliage.client.render.impl.primitives.FaceCrossedQuads;
import mods.betterfoliage.client.render.impl.primitives.SimpleOrientedQuad;
import mods.betterfoliage.common.config.Config;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BFAbstractRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderBlockSandWithCoral extends BFAbstractRenderer {

    public static int seedOffset = 2;
    
	public TextureSet coralCrustIcons = new TextureSet("bettergrassandleaves", "blocks/better_crust_%d");
	public TextureSet coralCrossIcons = new TextureSet("bettergrassandleaves", "blocks/better_coral_%d");
	public NoiseGeneratorSimplex noise;
	
    public boolean isBlockEligible(IBlockAccess blockAccess, IBlockState blockState, BlockPos pos) {
    	if (!Config.coralEnabled) return false;
	    if (noise == null) return false;
	    
	    if (blockState.getBlock() != Blocks.sand) return false;
	    if (!Config.coralBiomeList.contains(blockAccess.getBiomeGenForCoords(pos).biomeID)) return false;
	    
	    int terrainVariation = MathHelper.floor_double((noise.func_151605_a(pos.getX() * 0.1, pos.getZ() * 0.1) + 1.0) * 32.0);
	    if (terrainVariation >= Config.coralPopulation) return false;
	    
	    return true;
    }

	@Override
    public boolean renderBlock(BlockRendererDispatcher dispatcher, IBlockAccess blockAccess, IBlockState blockState, BlockPos pos, WorldRenderer worldRenderer, boolean useAO, EnumWorldBlockLayer layer) {
		if (layer != EnumWorldBlockLayer.CUTOUT_MIPPED) return false;
	        
		Double3 blockCenter = new Double3(pos).add(0.5, 0.5, 0.5);
		double offset = random.getRange(0.0, Config.coralVOffset, getSemiRandomFromPos(pos, 1));
		int textureVariation = getSemiRandomFromPos(pos, 2);
		double halfSize = Config.coralSize * 0.5;
		double halfCrustSize = Config.coralCrustSize * 0.5;
		
		BlockShadingData shading = shadingData.get();
		shading.update(blockAccess, blockState.getBlock(), pos, useAO);
		for (EnumFacing face : EnumFacing.values()) {
			if (blockAccess.getBlockState(pos.offset(face)).getBlock().getMaterial() != Material.water) continue;
			if (!Config.coralShallowWater && blockAccess.isAirBlock(pos.offset(face).up())) continue;
				
			
			if (textureVariation < Config.coralChance) {
			    SimpleOrientedQuad coralCrust = SimpleOrientedQuad.create(blockCenter.add(new Double3(face).scale(0.5 + offset)), face, halfCrustSize);
			    FaceCrossedQuads coral = FaceCrossedQuads.createTranslated(blockCenter.add(new Double3(face).scale(0.5)), face, random.getCircleXZ(Config.coralHOffset, textureVariation), halfSize, halfSize);
			    
			    coralCrust.setTexture(coralCrustIcons.get(textureVariation + face.ordinal()), textureVariation + face.ordinal()).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
			    coral.setTexture(coralCrossIcons.get(textureVariation + face.ordinal()), 0).setBrightness(shading).setColor(shading, Color4.opaqueWhite).render(worldRenderer);
			}
		}
		return true;
	}
	
    @SubscribeEvent
    public void handleTextureReload(TextureStitchEvent.Pre event) {
        coralCrustIcons.registerSprites(event.map);
        coralCrossIcons.registerSprites(event.map);
        BetterFoliage.log.info(String.format("Found %d coral crust textures", coralCrustIcons.numLoaded));
        BetterFoliage.log.info(String.format("Found %d coral textures", coralCrossIcons.numLoaded));
    }
    
    @SubscribeEvent
    public void handleWorldLoad(WorldEvent.Load event) {
        noise = new NoiseGeneratorSimplex(new Random(event.world.getWorldInfo().getSeed() + seedOffset));
    }
    
}
