package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashSet;

import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

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
