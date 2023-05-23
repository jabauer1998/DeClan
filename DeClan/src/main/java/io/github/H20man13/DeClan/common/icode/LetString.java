package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.StrExp;

public class LetString implements ICode {
	public String place;
	public StrExp value;
	
	public LetString(String place, StrExp value) {
		this.place = place;
		this.value = value;
	}

	public LetString(String place, String value){
		this.place = place;
		this.value = new StrExp(value);
	}

	@Override
	public String toString() {
		return place + " := " + value.toString();
	}
}
