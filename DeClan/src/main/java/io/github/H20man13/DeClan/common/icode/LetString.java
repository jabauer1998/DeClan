package io.github.H20man13.DeClan.common.icode;

public class LetString implements ICode {
	public String place;
	public String value;
	
	public LetString(String place, String value) {
		this.place = place;
		this.value = value;
	}

	@Override
	public String toString() {
		return place + " := \"" + value + "\"";
	}
}
