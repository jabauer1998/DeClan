package io.github.H20man13.DeClan.common.icode.exp;

import java.util.List;

public class CallExp implements Exp {
    public String functionName;
    public List<String> paramaters;

    public CallExp(String functionName, List<String> paramaters){
        this.functionName = functionName;
        this.paramaters = paramaters;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("CALL ");
		sb.append(functionName);
		sb.append(" ( ");
		boolean first = true;
		for (String arg : this.paramaters) {
			if (first) {
				first = false;
			} else {
				sb.append(" , ");
			}
			sb.append(arg);
		}
		sb.append(" )");
        return sb.toString();
    }

    @Override
    public boolean isBranch() {
        return true;
    }

    @Override
    public boolean isConstant() {
        return false;
    }
}
