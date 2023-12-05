package io.github.H20man13.DeClan.common.icode.label;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public abstract class Label implements ICode {
	public String label;

	protected Label(String label) {
		this.label = label;
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
	public abstract boolean equals(Object obj);
}
