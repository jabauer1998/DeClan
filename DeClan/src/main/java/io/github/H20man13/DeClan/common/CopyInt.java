package io.github.H20man13.DeClan.common;

import java.util.Objects;

public class CopyInt implements Copyable<CopyInt> {
	private int val;
	
	public CopyInt(int val) {
		this.val = val;
	}
	
	public String toString() {
		return Integer.toString(val);
	}
	
	public int hashCode() {
		return Objects.hash(val);
	}
	
	public int asInt() {
		return val;
	}

	@Override
	public CopyInt copy() {
		return new CopyInt(val);
	}
}
