package io.github.H20man13.DeClan.common.icode.exp;

import io.github.H20man13.DeClan.common.Copyable;

public interface NullableExp extends Copyable<NullableExp> {
	@Override
	public NullableExp copy();
	public boolean isConstant();
	public boolean isZero();
}
