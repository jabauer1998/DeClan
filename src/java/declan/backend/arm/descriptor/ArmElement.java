package io.github.h20man13.DeClan.common.arm.descriptor;

import io.github.h20man13.DeClan.common.Copyable;

public interface ArmElement extends Copyable<ArmElement> {
	public String toString();
	public boolean equals(Object other);
	public int hashCode();
	public ArmElement copy();
}
