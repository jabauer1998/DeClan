package io.github.H20man13.DeClan.common.icode.label;

import java.util.List;

import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.pat.P;

public abstract class Label implements ICode {
	public String label;

	protected Label(String label) {
		this.label = label;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public boolean isBranch() {
		return false;
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract List<ICode> genFlatCode();

	@Override
	public void replaceLabel(String from, String to){
		if(this.label.equals(from))
			this.label = to;
	}

	@Override
	public boolean containsLabel(String label){
		return this.label.equals(label);
	}

	@Override
	public boolean containsPlace(String place){
		return false;
	}

	@Override
	public void replacePlace(String from, String to){
		//Do nothing
	}
}
