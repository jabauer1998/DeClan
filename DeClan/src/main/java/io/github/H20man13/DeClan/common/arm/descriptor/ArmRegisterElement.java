package io.github.H20man13.DeClan.common.arm.descriptor;

public class ArmRegisterElement implements ArmElement {
	private String label;
	
	public ArmRegisterElement(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
	
	public int hashCode() {
		return label.hashCode();
	}
	
	public boolean equals(Object obj){
		if(obj instanceof ArmRegisterElement) {
			ArmRegisterElement reg = (ArmRegisterElement)obj;
			return this.label.equals(reg.label);
		}
		return false;
	}
}
