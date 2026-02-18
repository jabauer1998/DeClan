package declan.middleware.region;

import declan.middleware.icode.ICode;

public class InstructionRegion implements RegionBase {
	public ICode instruction;
	
	public InstructionRegion(ICode instruction) {
		this.instruction = instruction;
	}

	@Override
	public ICode getFirstInstruction() {
		return instruction;
	}

	@Override
	public ICode getLastInstruction() {
		return instruction;
	}

	@Override
	public RegionBase copy() {
		return new InstructionRegion(instruction.copy());
	}
}
