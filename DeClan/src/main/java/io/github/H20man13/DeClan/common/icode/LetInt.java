package io.github.H20man13.DeClan.common.icode;

public class LetInt implements ICode {
	private String place;
	private int value;
	
	public LetInt(String place, int value) {
		this.place = place;
		this.value = value;
	}

	@Override
	public String toString() {
		return place + " := " + value;
	}
}
