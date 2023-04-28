package io.github.H20man13.DeClan.common.icode;

import io.github.H20man13.DeClan.common.icode.ICode;

import java.util.List;

public class Call implements ICode {
	public String pname;
	public List<String> args;

	public Call(String pname, List<String> args) {
		this.pname = pname;
		this.args = args;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CALL " + pname + "(");
		boolean first = true;
		for (String arg : args) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(arg);
		}
		sb.append(")");
		return sb.toString();
	}
}
