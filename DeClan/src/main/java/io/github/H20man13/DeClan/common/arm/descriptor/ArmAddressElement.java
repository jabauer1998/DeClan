package io.github.H20man13.DeClan.common.arm.descriptor;

import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.ConversionUtils;

public class ArmAddressElement implements ArmElement {
	private String label;
	private ICode.Type type;
	
	public ArmAddressElement(String label, ICode.Type type) {
		this.label = label;
		this.type = type;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof ArmAddressElement) {
			ArmAddressElement elem = (ArmAddressElement)other;
			return elem.label.equals(label);
		}
		return false;
	}
	
	public int hashCode() {
		return label.hashCode();
	}
	
	public String toString(){
		return label;
	}

	@Override
	public ArmElement copy() {
		return new ArmAddressElement(label, type);
	}

	public Tuple<CopyStr, ICode.Type> toTuple() {
		return new Tuple<CopyStr, ICode.Type>(ConversionUtils.newS(label), type);
	}
}
