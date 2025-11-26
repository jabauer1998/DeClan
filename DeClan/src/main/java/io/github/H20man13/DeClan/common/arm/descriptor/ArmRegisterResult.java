package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashSet;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public class ArmRegisterResult {
	private HashSet<Tuple<String, String>> tupSet;
	
	public ArmRegisterResult() {
		this.tupSet = new HashSet<Tuple<String, String>>();
	}
	
	public void addResult(String ident, String reg) {
		this.tupSet.add(new Tuple<>(ident, reg));
	}
	
	public String getRegister(String addrName) {
		for(Tuple<String, String> tup : tupSet) {
			if(tup.source.equals(addrName))
				return tup.dest.toString();
		}
		return null;
	}
	
	public boolean containsRegister(String addrName) {
		for(Tuple<String, String> tup: tupSet) {
			if(tup.source.equals(addrName))
				return true;
		}
		return false;
	}
}
