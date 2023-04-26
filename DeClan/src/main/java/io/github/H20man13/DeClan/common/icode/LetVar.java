package io.github.H20man13.DeClan.common.icode;

public class LetVar implements ICode {
	private String place;
	private String var;

	public LetVar(String place, String var) {
		this.place = place;
		this.var = var;
	}

	@Override
	public String toString() {
		return place + " := " + var;
	}
}
