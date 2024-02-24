package io.github.H20man13.DeClan.common.icode.procedure;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public class Call implements ICode {
	public String pname;
	public List<Assign> params;

	public Call(String pname, List<Assign> params) {
		this.pname = pname;
		this.params = params;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CALL " + pname + "(");

		boolean first = true;
		for (Assign param : params) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(param.toString());
		}
		sb.append(")");
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
		return P.PAT(P.CALL(), P.ID());
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Call){
			Call objCall = (Call)obj;

			if(!pname.equals(objCall.pname))
				return false;

			if(objCall.params.size() != params.size())
				return false;

			for(int i = 0; i < params.size(); i++){
				Assign arg1 = objCall.params.get(i);
				Assign arg2 = params.get(i);

				if(!arg1.equals(arg2))
					return false;
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<ICode> genFlatCode() {
		LinkedList<ICode> resultList = new LinkedList<ICode>();
		resultList.add(this);
		return resultList;
	}
}
