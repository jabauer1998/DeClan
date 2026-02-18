package declan.middleware.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import declan.utils.Tuple;
import declan.utils.pat.P;

public class Call extends ICode {
	private static Set<Call> calls = new HashSet<Call>();
	
	public static void resetCalls() {
		calls = new HashSet<Call>();
	}
	
	public String pname;
	public List<Def> params;
	private int seqNum;

	public Call(String pname, List<Def> params) {
		this.pname = pname;
		this.params = params;
		recalclulateIdentNumber();
	}
	
	public void recalclulateIdentNumber() {
		for(this.seqNum = 0; calls.contains(this); ++this.seqNum);
		for(Def param: params) {
			param.recalculateIdentNumber();
		}
		calls.add(this);
	}
	
	private Call(Call other) {
		this.pname = other.pname;
		LinkedList<Def> params = new LinkedList<Def>();
		for(Def param: other.params) {
			params.add((Def)param.copy());
		}
		this.params = params;
		this.seqNum = other.seqNum;
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
		sb.append(")");
		return sb.toString();
	}
	
	public String toAccurateString() {
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
		sb.append(") NUMBER ");
		sb.append(seqNum);
		
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
			
			if(seqNum != objCall.seqNum)
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
	}

	@Override
	public void replaceLabel(String from, String to) {
		if(pname.equals(from))
			pname = to;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pname, params, seqNum);
	}

	@Override
	public ICode copy() {
		return new Call(this);
	}
}
