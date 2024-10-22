package io.github.H20man13.DeClan.common.region;

import io.github.H20man13.DeClan.common.icode.ICode;

public class InstructionRegion implements RegionBase {
	public ICode instruction;
	
	public InstructionRegion(ICode instruction) {
		this.instruction = instruction;
	}
}
