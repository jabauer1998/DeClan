package io.github.H20man13.DeClan.common.icode;

public class Goto implements ICode {
	private String label;

	public Goto(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "GOTO " + label;
	}
}
