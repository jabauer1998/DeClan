package io.github.H20man13.DeClan.common.icode;

import java.util.List;

public class Proc implements ICode {
	public String pname;
	public List<String> params;

	public Proc(String pname, List<String> params) {
		this.pname = pname;
		this.params = params;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PROC " + pname + " ( ");
		boolean first = true;
		for (String param : params) {
			if (first) {
				first = false;
			} else {
				sb.append(" , ");
			}
			sb.append(param);
		}
		sb.append(" )");
		return sb.toString();
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
