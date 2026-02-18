package io.github.h20man13.DeClan.common.region;

import java.util.List;

import io.github.h20man13.DeClan.common.flow.BlockNode;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.If;
import io.github.h20man13.DeClan.common.icode.exp.BinExp;

public class LoopBodyRegion extends Region {
	public LoopBodyRegion(RegionBase dest, List<RegionBase> subRegions) {
		super(dest, subRegions);
	}
	
	public BinExp getLoopCondition() {
		RegionBase base = this.getHeader();
		while(!(base instanceof BlockRegion)) {
			if(base instanceof Region) {
				base = ((Region)base).getHeader();
			}
		}
		
		BlockRegion baseBlock = (BlockRegion)base;
		If ifStat = (If)baseBlock.getLastInstruction();
		return ifStat.exp;
	}
}
