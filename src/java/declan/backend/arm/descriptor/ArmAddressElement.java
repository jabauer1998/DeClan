package declan.backend.arm.descriptor;

import java.util.Objects;

import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.middleware.icode.ICode;
import declan.utils.ConversionUtils;

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
			if(elem.label.equals(label))
				return elem.type == type;
		}
		return false;
	}
	
	public int hashCode() {
		return Objects.hash(label, type);
	}
	
	@Override
	public String toString(){
		return "(" + label + ", " + type + ")";
	}

	@Override
	public ArmElement copy() {
		return new ArmAddressElement(label, type);
	}

	public Tuple<CopyStr, ICode.Type> toTuple() {
		return new Tuple<CopyStr, ICode.Type>(ConversionUtils.newS(label), type);
	}
}
