package io.github.H20man13.DeClan.common.icode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.pat.P;

public class End extends ICode {
	@Override
	public String toString() {
		return "END";
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return true;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof End) 
			return true;

		return false;
	}

	@Override
	public P asPattern() {
		return P.END();
	}

	@Override
	public boolean containsPlace(String place) {
		return false;
	}

	@Override
	public boolean containsLabel(String label) {
		return false;
	}

	@Override
	public void replacePlace(String from, String to) {
		//Do nothing
	}

	@Override
	public void replaceLabel(String from, String to) {
		//Do nothing
	}
	
	@Override
	public int hashCode() {
		return 42;
	}

	@Override
	public ICode copy() {
		return new End();
	}
}
