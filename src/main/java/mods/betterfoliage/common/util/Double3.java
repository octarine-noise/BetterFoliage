package mods.betterfoliage.common.util;

public class Double3 {

	public final double x;
	public final double y;
	public final double z;
	
	public Double3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Double3 add(Double3 other) {
		return new Double3(x + other.x, y + other.y, z + other.z);
	}
	
	public Double3 scaleAxes(double sx, double sy, double sz) {
		return new Double3(x * sx, y * sy, z * sz);
	}
	
	public Double3 scale(double s) {
		return new Double3(x * s, y * s, z * s);
	}
	
	public Double3 inverse() {
		return new Double3(-x, -y, -z);
	}
}
