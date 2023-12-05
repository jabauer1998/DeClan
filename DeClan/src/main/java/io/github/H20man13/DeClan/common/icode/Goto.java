package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

public class Goto implements ICode {
	public String label;

	public Goto(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "GOTO " + label;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Goto){
			Goto objGoto = (Goto)obj;

			return objGoto.label.equals(label);
		} else {
			return false;
		}
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return true;
	}

	@Override
	public P asPattern() {
		return P.PAT(P.GOTO(), P.ID());
	}
}
