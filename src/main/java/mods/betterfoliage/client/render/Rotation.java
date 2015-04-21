package mods.betterfoliage.client.render;

import net.minecraft.util.EnumFacing;
import mods.betterfoliage.client.misc.Double3;

/** Represents a series of rotation transformations around the primary axes by 90deg, i.e. a permutation of cardinal axes   
 * @author octarine-noise
 */
public class Rotation {

	/** No rotation */
	public static final Rotation identity = new Rotation(0, false, 1, false, 2, false);
	
	/** {@link Rotation}s in the positive direction along the X, Y, Z axes (in order) */
	public static final Rotation[] rotatePositive = new Rotation[]{
		new Rotation(0, false, 2, false, 1, true),
		new Rotation(2, true, 1, false, 0, false),
		new Rotation(1, false, 0, true, 2, false)
	};
	
	/* Scramble arrays for the axis directions and faces 
	  
	   index: ordinal of the transformed Axis
	   axis[]: ordinal of the original Axis
	   flipped[]: positive directions of axes match up
	*/
	
	/** World axis indexed by local axis */
	protected byte[] axis = new byte[3];
	
	/** Do the two axes point in opposite directions? */
	protected boolean[] flipped = new boolean[3];
	
	/** World facing indexed by local facing */
	protected EnumFacing[] facings = new EnumFacing[6];

	public Rotation() {}
	
	public Rotation(int xAxis, boolean xPos, int yAxis, boolean yPos, int zAxis, boolean zPos) {
		axis = new byte[]{(byte) xAxis, (byte) yAxis, (byte) zAxis};
		flipped = new boolean[]{xPos, yPos, zPos};
		cacheFaces();
	}
	
	/** Apply this rotation to the given vector
	 * @param vector
	 * @return rotated vector
	 */
	public Double3 transform(Double3 vector) {
		double[] in = new double[]{vector.x, vector.y, vector.z};
		double[] out = new double[3];
		for (int localAxis = 0; localAxis < 3; localAxis ++) {
			byte worldAxis = axis[localAxis];
			out[worldAxis] = flipped[localAxis] ? -in[localAxis] : in[localAxis];
		}
		return new Double3(out[0], out[1], out[2]);
	}
	
	public EnumFacing transform(EnumFacing facing) {
		return facings[facing.ordinal()];
	}
	
	/** Return a new {@link Rotation} object that is equivalent to applying
	 *  this rotation, then the parameter
	 * @param other
	 * @return composite rotation
	 */
	public Rotation apply(Rotation other) {
		Rotation result = new Rotation();
		for (int idx = 0; idx < 3; idx ++) {
			result.axis[idx] = axis[other.axis[idx]];
			result.flipped[idx] = other.flipped[idx] ^ flipped[other.axis[idx]];
		}
		result.cacheFaces();
		return result;
	}

	/** Return a rotation that is the inverse transformation to this one
	 * @return
	 */
	public Rotation invert() {
		Rotation result = new Rotation();
		for (int idx = 0; idx < 3; idx ++) {
			result.axis[axis[idx]] = (byte) idx;
			result.flipped[axis[idx]] = flipped[idx];
		}
		result.cacheFaces();
		return result;
	}
	
	protected void cacheFaces() {
		for (EnumFacing localFacing : EnumFacing.values()) for (EnumFacing worldFacing : EnumFacing.values()) {
			byte localAxis = (byte) localFacing.getAxis().ordinal();
			byte worldAxis = (byte) worldFacing.getAxis().ordinal();
			if (axis[localAxis] == worldAxis && flipped[localAxis] == (worldFacing.getAxisDirection() != localFacing.getAxisDirection())) {
				facings[localFacing.ordinal()] = worldFacing;
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int idx = 0; idx < 3; idx++) {
			sb.append(EnumFacing.Axis.values()[idx].toString());
			sb.append("+ => ");
			sb.append(EnumFacing.Axis.values()[axis[idx]]);
			sb.append(flipped[idx] ? "-" : "+");
			sb.append("\n");
		}
		for (EnumFacing face : EnumFacing.values()) {
			sb.append(face.toString());
			sb.append(" => ");
			sb.append(facings[face.ordinal()].toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
