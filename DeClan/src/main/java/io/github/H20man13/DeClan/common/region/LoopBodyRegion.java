package io.github.H20man13.DeClan.common.region;

import java.util.List;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.icode.ICode;

public class LoopBodyRegion extends Region {
	public LoopBodyRegion(RegionBase dest, List<RegionBase> subRegions) {
		super(dest, subRegions);
	}
	
	public ICode getLoopCondition() {
		RegionBase base = this.getHeader();
		while(!(base instanceof BlockRegion)) {
			if(base instanceof Region) {
				base = ((Region)base).getHeader();
			}
		}
		
		BlockRegion baseBlock = (BlockRegion)base;
		return baseBlock.getLastInstruction();
	}
}
