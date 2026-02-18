package declan.backend.arm.descriptor;

import declan.utils.Copyable;

public interface ArmElement extends Copyable<ArmElement> {
	public String toString();
	public boolean equals(Object other);
	public int hashCode();
	public ArmElement copy();
}
