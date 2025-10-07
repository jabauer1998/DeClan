package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashSet;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;

public class ArmRegisterResult {
	private HashSet<Tuple<IdentExp, ArmRegisterElement>> tupSet;
	
	public ArmRegisterResult() {
		this.tupSet = new HashSet<Tuple<IdentExp, ArmRegisterElement>>();
	}
	
	public void addResult(IdentExp ident, String reg) {
		this.tupSet.add(new Tuple<>(ident, new ArmRegisterElement(reg)));
	}
	
	public String getRegister(String addrName) {
		for(Tuple<IdentExp, ArmRegisterElement> tup : tupSet) {
			if(tup.source.ident.equals(addrName))
				return tup.dest.toString();
		}
		return null;
	}
}
