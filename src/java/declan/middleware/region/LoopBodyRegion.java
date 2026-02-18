package declan.middleware.region;

import java.util.List;

import declan.utils.flow.BlockNode;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.exp.BinExp;

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
