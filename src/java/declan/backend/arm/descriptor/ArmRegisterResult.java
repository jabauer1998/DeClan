package declan.backend.arm.descriptor;

import java.util.HashSet;

import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.middleware.icode.exp.IdentExp;
import declan.utils.ConversionUtils;

public class ArmRegisterResult {
	private HashSet<Tuple<CopyStr, CopyStr>> tupSet;
	
	public ArmRegisterResult() {
		this.tupSet = new HashSet<Tuple<CopyStr, CopyStr>>();
	}
	
	public void addResult(String ident, String reg) {
		this.tupSet.add(new Tuple<>(ConversionUtils.newS(ident), ConversionUtils.newS(reg)));
	}
	
	public String getRegister(String addrName) {
		for(Tuple<CopyStr, CopyStr> tup : tupSet) {
			if(tup.source.toString().equals(addrName))
				return tup.dest.toString();
		}
		return null;
	}
	
	public boolean containsRegister(String addrName) {
		for(Tuple<CopyStr, CopyStr> tup: tupSet) {
			if(tup.source.toString().equals(addrName))
				return true;
		}
		return false;
	}
	
	public String toString() {
		return this.tupSet.toString();
	}
}
