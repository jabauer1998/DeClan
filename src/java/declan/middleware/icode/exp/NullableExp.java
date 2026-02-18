package declan.middleware.icode.exp;

import declan.utils.Copyable;

public interface NullableExp extends Copyable<NullableExp> {
	@Override
	public NullableExp copy();
	public boolean isConstant();
	public boolean isZero();
}
