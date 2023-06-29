package io.github.H20man13.DeClan.common.icode.exp;

import java.util.List;

import io.github.H20man13.DeClan.common.pat.P;
import io.github.H20man13.DeClan.common.pat.P.ID;

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

    @Override
    public P asPattern(boolean hasContainer) {
        if(hasContainer){
            return P.PAT(P.CALL(), P.ID());
        } else {
            return null;
        }
    }
}
