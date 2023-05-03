package io.github.H20man13.DeClan.common.icode;

import edu.depauw.declan.common.ast.BinaryOperation;
import io.github.H20man13.DeClan.main.MyTypeChecker;

public class LetBin implements ICode {
	public String place;
	public String left;
	public Op op;
	public String right;

	public LetBin(String place, String left, Op op, String right) {
		this.place = place;
		this.left = left;
		this.op = op;
		this.right = right;
	}
	
	@Override
	public String toString() {
		return place + " := " + left + " " + op + " " + right;
	}

	public enum Op {
	    ADD, SUB, MUL, DIV, MOD, BAND, BOR, LT, LE, GT, GE, NE, EQ
	}
}
