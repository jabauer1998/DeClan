package io.github.H20man13.DeClan.common.arm.descriptor;

public class ArmAddressElement implements ArmElement {
	private String label;
	
	public ArmAddressElement(String label) {
		this.label = label;
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
}
