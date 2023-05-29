package io.github.H20man13.DeClan.common.icode;

public class Return implements ICode {
	@Override
	public String toString() {
		return "RETURN";
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return true;
	}
}
