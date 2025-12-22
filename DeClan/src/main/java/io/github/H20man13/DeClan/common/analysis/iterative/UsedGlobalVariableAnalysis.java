package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.util.Utils;

public class UsedGlobalVariableAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<CopyStr, NullableExp>>>, HashSet<Tuple<CopyStr, NullableExp>>, Tuple<CopyStr, NullableExp>> {

	private Map<ICode, HashSet<Tuple<CopyStr, NullableExp>>> map;
	
	public UsedGlobalVariableAnalysis(FlowGraph flowGraph, Config cfg) {
		super(flowGraph, Direction.FORWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		map = newMap();
		for(BlockNode node: flowGraph) {
			for(ICode instr: node) {
				HashSet<Tuple<CopyStr, NullableExp>> hash = newSet();
				if(instr instanceof Def) {
					Def def = (Def)instr;
					if(def.scope == Scope.GLOBAL && def.isConstant())
						hash.add(new Tuple<CopyStr, NullableExp>(new CopyStr(def.label), def.val));
				}
				map.put(instr, hash);
			}
		}
	}

	@Override
	public HashSet<Tuple<CopyStr, NullableExp>> transferFunction(ICode instr,
			HashSet<Tuple<CopyStr, NullableExp>> inputSet) {
		HashSet<Tuple<CopyStr, NullableExp>> newSet = newSet();
		
		if(instr instanceof CodeSec) {
			//Return the Null new Set
			return newSet;
		} else if(instr instanceof ProcSec){
			return newSet;
		}	else {
			newSet.addAll(map.get(instr));
			newSet.addAll(inputSet);
			return newSet;
		}
	}
	
}
