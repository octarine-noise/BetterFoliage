package mods.betterfoliage.client.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Source for random perturbation vectors. Uses a pre-filled array[64] of values for performance and consistency reasons. 
 * @author octarine-noise
 */
@SideOnly(Side.CLIENT)
public class PerturbationSource {

	/** Number of discrete variations (rotation steps) to calculate. Should be a power of 2. */
	public static final int STEPS = 64;
	
	/** Mask to apply (bitwise AND) to integers to get an array index. */
	public static final int MASK = 63;
	
    /** Random vector pool. Unit rotation vectors in the XZ plane, Y coord goes between [-1.0, 1.0].
     * Filled at init time */
    public Double3[] pRot = new Double3[STEPS];
    
    /** Pool of random double values. Filled at init time. */
    public double[] pRand = new double[STEPS];
    
    public PerturbationSource() {
        List<Double3> perturbs = new ArrayList<Double3>(STEPS);
        for (int idx = 0; idx < STEPS; idx++) {
            double angle = (double) idx * Math.PI * 2.0 / ((double) STEPS);
            perturbs.add(new Double3(Math.cos(angle), Math.random() * 2.0 - 1.0, Math.sin(angle)));
            pRand[idx] = Math.random();
        }
        Collections.shuffle(perturbs);
        Iterator<Double3> iter = perturbs.iterator();
        for (int idx = 0; idx < STEPS; idx++) pRot[idx] = iter.next();
    }
    
    /** Returns a random vector.<br/>
     * XZ coords are a random point on a circle with origin (0,0,0) and given radius.<br/>
     * Y coord is a random value between (-halfHeight, +halfHeight).
     * @param radius circle radius
     * @param halfHeight Y coord amplitude
     * @param variation index into random pool
     * @return random vector
     */
    public Double3 getCylinderXZY(double radius, double halfHeight, int variation) {
        return pRot[variation & MASK].scaleAxes(radius, halfHeight, radius);
    }
    
    /** Same as {@link PerturbationSource#getCylinderXZY(double, double, int)} with Y amplitude of 0.
     * @param radius circle radius
     * @param variation index into random pool
     * @return 
     */
    public Double3 getCircleXZ(double radius, int variation) {
        return getCylinderXZY(radius, 0, variation);
    }
    
    /** Returns a random value between (min, max).
     * @param min
     * @param max
     * @param variation index into random pool
     * @return random value
     */
    public double getRange(double min, double max, int variation) {
        return min + (max - min) * pRand[variation & MASK];
    }
}
