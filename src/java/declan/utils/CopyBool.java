package io.github.h20man13.DeClan.common;

import java.util.Objects;

public class CopyBool implements Copyable<CopyBool>{
	private boolean tf;
	
	public CopyBool(boolean tf) {
		this.tf = tf;
	}
	
	public boolean asBool() {
		return tf;
	}

	@Override
	public CopyBool copy() {
		return new CopyBool(tf);
	}
	
	@Override
	public String toString() {
		return tf ? "TRUE" : "FALSE";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tf);
	}
	
	@Override
	public boolean equals(Object eq) {
		if(eq instanceof CopyBool) {
			CopyBool bool = (CopyBool)eq;
			if(bool.tf == tf)
				return true;
		}
		return false;
	}
}
