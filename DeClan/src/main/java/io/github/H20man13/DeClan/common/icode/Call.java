package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.pat.P;

public class Call implements ICode {
	public String pname;
	public List<Def> params;
	private ICode from;

	public Call(String pname, List<Def> params, ICode from) {
		this.pname = pname;
		this.params = params;
		this.from = from;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CALL " + pname + "(");

		boolean first = true;
		for (Def param : params) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(param.toString());
		}
		sb.append(") FROM ");
		sb.append(from.toString());
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
				Def arg1 = objCall.params.get(i);
				Def arg2 = params.get(i);

				if(!arg1.equals(arg2))
					return false;
			}
			
			if(!from.equals(objCall.from))
				return false;
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean containsPlace(String place){
		for(Def assign: params)
			if(assign.containsPlace(place))
				return true;

		return false;
	}

	@Override
	public boolean containsLabel(String label) {
		if(pname.equals(label))
			return true;
		return false;
	}

	@Override
	public void replacePlace(String from, String to) {
		for(Def assign: params)
			assign.replacePlace(from, to);
		this.from.replacePlace(from, to);
	}

	@Override
	public void replaceLabel(String from, String to) {
		if(pname.equals(from))
			pname = to;
		this.from.replaceLabel(from, to);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pname, params, from);
	}

	@Override
	public ICode copy() {
		List<Def> newParams = new LinkedList<Def>();
		for(Def param: params) {
			newParams.add((Def)param.copy());
		}
		return new Call(pname, newParams, from.copy());
	}
}
