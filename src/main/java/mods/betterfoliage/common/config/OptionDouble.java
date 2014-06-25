package mods.betterfoliage.common.config;

public class OptionDouble {

	public double min;
	public double max;
	public double step;
	public double value;
	
	public OptionDouble(double min, double max, double step, double value) {
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = value;
	}
	
	public void increment() {
		value += step;
		if (value > max) value = max;
	}
	
	public void decrement() {
		value -= step;
		if (value < min) value = min;
	}
}
