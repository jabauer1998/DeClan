package io.github.H20man13.DeClan.common.region;

import java.util.LinkedList;

import io.github.H20man13.DeClan.common.flow.BasicBlock;

public class RootRegion extends Region {
	private BasicBlock block;
	
	public RootRegion(BasicBlock block) {
		super(null, new LinkedList<Region>());
		this.block = block;
	}
}
