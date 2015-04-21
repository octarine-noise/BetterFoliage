package mods.betterfoliage.client.render;

import mods.betterfoliage.client.misc.PerturbationSource;
import mods.betterfoliage.client.render.impl.primitives.IQuadCollection;

/** Set of pre-rendered models to speed up world rendering.
 * Usually geometry is kept fixed, while UV and color/brightness data is overwritten during rendering.
 * @author octarine-noise
 * @param <Q> type of {@link IQuadCollection} used
 */
public abstract class RenderCache<Q extends IQuadCollection> {

	Rotation[] rotations;
	ThreadLocal<? extends IShadingData>[] shadings;
	IQuadCollection[][] quads;
	
	/**
	 * @param shadingData shading data for the standard orientation
	 * @param rotations set of rotations to keep models for
	 */
	@SuppressWarnings("unchecked")
	public RenderCache(ThreadLocal<? extends IShadingData> shadingData, Rotation... rotations) {
		this.rotations = rotations;
		shadings = new ThreadLocal[rotations.length];
		quads = new IQuadCollection[rotations.length][PerturbationSource.STEPS];
		for(int idx = 0; idx < rotations.length; idx++) {
			shadings[idx] = (rotations[idx] == Rotation.identity) ? shadingData : RotatedShadingData.wrap(shadingData, rotations[idx]);
		}
	}
	
	/** Redraw all cached models */
	public void reinit() {
		for(int rotation = 0; rotation < rotations.length; rotation++) {
			for(int variation = 0; variation < PerturbationSource.STEPS; variation++) {
				quads[rotation][variation] = drawQuads(variation).transform(rotations[rotation]);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Q getQuads(int rotation, int variation) {
		return (Q) quads[rotation][variation];
	}
	
	public IShadingData getShading(int rotationIndex) {
		return shadings[rotationIndex].get();
	}
	
	public abstract Q drawQuads(int variation);
	
}
