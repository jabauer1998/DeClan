package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.main.MyTypeChecker;

public class LetUn implements ICode {
	public String place;
	public Op op;
	public String value;

	public LetUn(String place, Op op, String value) {
		this.place = place;
		this.op = op;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return place + " := " + op + " " + value;
	}

    public enum Op {
		NEG, BNOT
    }
}
