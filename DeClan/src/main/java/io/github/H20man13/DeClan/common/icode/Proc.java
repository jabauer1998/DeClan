package io.github.H20man13.DeClan.common.icode;

import java.util.List;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.pat.P;

public class Proc implements ICode {
	public String pname;
	public List<Tuple<String, String>> params;

	public Proc(String pname, List<Tuple<String, String>> params) {
		this.pname = pname;
		this.params = params;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PROC " + pname + " ( ");
		boolean first = true;
		for (Tuple<String, String> param : params) {
			if (first) {
				first = false;
			} else {
				sb.append(" , ");
			}
			sb.append(param.source);
			sb.append(" -> ");
			sb.append(param.dest);
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

	@Override
	public P asPattern() {
		return P.PAT(P.PROC(), P.ID());
	}
}
