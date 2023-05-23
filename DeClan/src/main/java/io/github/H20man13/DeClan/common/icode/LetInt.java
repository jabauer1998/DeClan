package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.IntExp;

public class LetInt implements ICode {
	public String place;
	public IntExp value;
	
	public LetInt(String place, IntExp value){
		this.place = place;
		this.value = value;
	}

	public LetInt(String place, int value){
		this.place = place;
		this.value = new IntExp(value);
	}

	@Override
	public String toString() {
		return place + " := " + value.toString();
	}
}
