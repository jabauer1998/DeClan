package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

public class Label implements ICode {
	public String label;

	public Label(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "LABEL " + label;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return false;
	}

	@Override
	public P asPattern() {
		return P.PAT(P.LABEL(), P.ID());
	}
}
