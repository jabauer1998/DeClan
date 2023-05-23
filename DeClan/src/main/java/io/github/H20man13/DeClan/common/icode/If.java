package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.exp.BinExp;

public class If implements ICode {
	public BinExp exp;
	public String ifTrue, ifFalse;

	public If(BinExp exp, String ifTrue, String ifFalse) {
		this.ifTrue = ifTrue;
		this.ifFalse = ifFalse;
	}
	
	@Override
	public String toString() {
		return "IF " + exp.toString() + " THEN " + ifTrue + " ELSE " + ifFalse;
	}
}
