package declan.middleware.region;

import declan.utils.Copyable;
import declan.middleware.icode.ICode;

public interface RegionBase extends Copyable<RegionBase> {
	@Override
	public String toString();
	@Override
	public int hashCode();
	public ICode getFirstInstruction();
	public ICode getLastInstruction();
}
