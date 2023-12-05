package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.pat.P;

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

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Return)
			return true;
		else
			return false;
	}

	@Override
	public P asPattern() {
		// TODO Auto-generated method stub
		return P.RETURN();
	}
}
