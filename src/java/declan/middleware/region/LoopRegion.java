package io.github.h20man13.DeClan.common.region;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import io.github.h20man13.DeClan.common.CopyBool;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.icode.Assign;
import io.github.h20man13.DeClan.common.icode.Def;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.exp.BinExp;
import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.icode.exp.IntExp;
import io.github.h20man13.DeClan.common.util.ConversionUtils;

public class LoopRegion extends Region {
	public LoopRegion(LoopBodyRegion region) {
		super(region, genSubRegionFromBody(region));
	}
	
	private static LinkedList<RegionBase> genSubRegionFromBody(LoopBodyRegion body){
		LinkedList<RegionBase> subRegion = new LinkedList<RegionBase>();
		subRegion.add(body);
		return subRegion;
	}
	
	public BinExp getLoopCondition() {
		return ((LoopBodyRegion)this.subRegions.get(0)).getLoopCondition();
	}
	
	public boolean containsInductionVariable() {
		BinExp cond = getLoopCondition();
		
		boolean zeroExists = false;
		boolean otherIsConstant = false;
		
		Tuple<CopyBool, CopyBool> analyzed = containsInductionVariable(this.header, zeroExists, otherIsConstant, cond.left, cond.right);
		if(analyzed.source.asBool() == true)
			zeroExists = true;
		if(analyzed.dest.asBool() == true)
			otherIsConstant = true;
		
		if(!zeroExists || !otherIsConstant) {
			for (RegionBase reg: this.getEntryRegions()) {
				analyzed = containsInductionVariable(reg, zeroExists, otherIsConstant, cond.left, cond.right);
				if(analyzed.source.asBool() == true)
					zeroExists = true;
				if(analyzed.dest.asBool() == true)
					otherIsConstant = true;
				
				if(zeroExists && otherIsConstant)
					break;
			}
		}
		
		return zeroExists && otherIsConstant;
	}
	
	private static Tuple<CopyBool, CopyBool> containsInductionVariable(RegionBase reg, boolean zeroExists, boolean otherIsConstant, IdentExp left, IdentExp right){
		if(reg instanceof BlockRegion) {
			BlockRegion block = (BlockRegion)reg;
			for(int i = block.getNumberOfInstructions() - 1; i >= 0; i--) {
				ICode instruction = block.getInstruction(i);
				if(instruction instanceof Def){
					Def definition = (Def)instruction;
					if((definition.label.equals(left.ident) && definition.scope == left.scope) 
					||(definition.label.equals(right.ident) && definition.scope == right.scope)){
						if(definition.val instanceof IntExp) {
							IntExp exp = (IntExp)definition.val;
							if(exp.value == 0) {
								if(zeroExists == false)
									zeroExists = true;
								else
									otherIsConstant = true;
							} else {
								otherIsConstant = true;
							}
						}
					}
				} else if(instruction instanceof Assign){
					Assign assign = (Assign)instruction;
					if((assign.place.equals(left.ident) && assign.getScope() == left.scope) 
					||(assign.place.equals(right.ident) && assign.getScope() == right.scope)){
						if(assign.value instanceof IntExp) {
							IntExp exp = (IntExp)assign.value;
							if(exp.value == 0) {
								if(zeroExists == false)
									zeroExists = true;
								else
									otherIsConstant = true;
							} else {
								otherIsConstant = true;
							}
						}
					}
				}
				
				if(zeroExists && otherIsConstant)
					break;
			}
		} else {
			BaseRegion base = (BaseRegion)reg;
			
			for (RegionBase sub: base) {
				Tuple<CopyBool, CopyBool> analyzed = containsInductionVariable(sub, zeroExists, otherIsConstant, left, right);
				if(analyzed.source.asBool() == true)
					zeroExists = true;
				if(analyzed.dest.asBool() == true)
					otherIsConstant = true;
				
				if(zeroExists && otherIsConstant)
					break;
			}
		}
		return new Tuple<CopyBool, CopyBool>(ConversionUtils.newB(zeroExists), ConversionUtils.newB(otherIsConstant));
	}
}
