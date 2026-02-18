package io.github.h20man13.DeClan.common.region;

import io.github.h20man13.DeClan.common.Copyable;
import io.github.h20man13.DeClan.common.icode.ICode;

public interface RegionBase extends Copyable<RegionBase> {
	@Override
	public String toString();
	@Override
	public int hashCode();
	public ICode getFirstInstruction();
	public ICode getLastInstruction();
}
