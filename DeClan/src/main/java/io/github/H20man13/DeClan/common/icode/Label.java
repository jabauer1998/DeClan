package io.github.H20man13.DeClan.common.icode;

public class Label implements ICode {
	public String label;

	public Label(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "LABEL " + label;
	}
}
