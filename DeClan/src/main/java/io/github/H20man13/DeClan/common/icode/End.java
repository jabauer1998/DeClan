package io.github.H20man13.DeClan.common.icode;

public class End implements ICode {
	@Override
	public String toString() {
		return "END";
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return false;
	}
}
