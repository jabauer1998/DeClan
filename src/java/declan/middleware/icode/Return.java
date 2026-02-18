package declan.middleware.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import declan.utils.pat.P;

public class Return extends ICode {
	private String funcName;
	
	public Return(String funcName) {
		this.funcName = funcName;
	}

	@Override
	public String toString() {
		return "RETURN FROM " + funcName;
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
		if(obj instanceof Return) {
			Return ret = (Return)obj;
			return ret.funcName.equals(funcName);
		}
		return false;
	}

	@Override
	public P asPattern() {
		return P.PAT(P.RETURN(), P.FROM(), P.ID());
	}

	@Override
	public boolean containsPlace(String place) {
		return false;
	}

	@Override
	public boolean containsLabel(String label) {
		return false;
	}

	@Override
	public void replacePlace(String from, String to) {
		//Do nothing this is just a placeholder
	}

	@Override
	public void replaceLabel(String from, String to) {
		//Do nothing this is just a placeholder
	}
	
	@Override
	public int hashCode() {
		return funcName.hashCode();
	}

	@Override
	public ICode copy() {
		return new Return(funcName);
	}
}
