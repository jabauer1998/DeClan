package io.github.H20man13.DeClan.common.icode;

import java.util.Objects;

import io.github.H20man13.DeClan.common.pat.P;

public class Spill extends ICode {
	public String name;
	private ICode.Type type;
	
	public Spill(String name, ICode.Type type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "SPILL " + name + '<' + type + '>';
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
	public ICode copy() {
		return new Spill(name, type);
	}

	@Override
	public P asPattern() {
		if(type == ICode.Type.INT) return P.PAT(P.SPILL(), P.ID(), P.INT());
		else if(type == ICode.Type.BOOL) return P.PAT(P.SPILL(), P.ID(), P.BOOL());
		else if(type == ICode.Type.REAL) return P.PAT(P.SPILL(), P.ID(), P.REAL());
		else if(type == ICode.Type.STRING) return P.PAT(P.SPILL(), P.ID(), P.STR());
		else return P.PAT();
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof Spill) {
			Spill objSpill = (Spill)object;
			if(objSpill.name.equals(name))
				return objSpill.type == type;
		}
		return false;
	}

	@Override
	public boolean containsPlace(String place) {
		return name.equals(place);
	}

	@Override
	public boolean containsLabel(String label) {
		return false;
	}

	@Override
	public void replacePlace(String from, String to) {
		if(name.equals(from))
			name = to;
	}
	
	public String getAddr(){
		return this.name;
	}

	@Override
	public void replaceLabel(String from, String to) {
		// Do nothing
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getClass().getName(), name, type);
	}
}
