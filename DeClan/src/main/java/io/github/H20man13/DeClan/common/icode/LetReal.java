package io.github.H20man13.DeClan.common.icode;

public class LetReal implements ICode {
	public String place;
	public double value;
	
	public LetReal(String place, double value) {
		this.place = place;
		this.value = value;
	}

	@Override
	public String toString() {
		return place + " := " + value;
	}
}
