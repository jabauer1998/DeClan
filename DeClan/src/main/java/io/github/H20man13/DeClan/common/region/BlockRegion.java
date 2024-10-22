package io.github.H20man13.DeClan.common.region;

import java.util.LinkedList;
import java.util.List;

import io.github.H20man13.DeClan.common.flow.BasicBlock;

public class BlockRegion extends Region {
	private BasicBlock block;
	
	public BlockRegion(BasicBlock block, RegionBase header, List<RegionBase> subRegions) {
		super(header, subRegions);
		this.block = block;
	}
}
