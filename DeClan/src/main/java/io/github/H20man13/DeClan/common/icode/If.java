package io.github.H20man13.DeClan.common.icode;

public class If implements ICode {
	public String left, right, ifTrue, ifFalse;
	public Op op;

	public If(String left, Op op, String right, String ifTrue, String ifFalse) {
		this.left = left;
		this.op = op;
		this.right = right;
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}

	public If(String test, String ifTrue, String ifFalse) {
		this.left = test;
		this.op = Op.EQ;
		this.right = "TRUE";
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
	
	@Override
	public String toString() {
		return "IF " + left + " " + op + " " + right + ", " + ifTrue + ", " + ifFalse;
	}

	public enum Op {
	    EQ, GT, NE, GE, LE, LT
	}
}
