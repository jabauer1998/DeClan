package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.section.BssSec;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.DataSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.util.Utils;

public class ExpectedConstantAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>>>, HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>>, Tuple<ICode, Tuple<CopyStr, NullableExp>>> {
private Map<ICode, HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>>> map;
	
	public ExpectedConstantAnalysis(FlowGraph flowGraph, Config cfg) {
		super(flowGraph, Direction.BACKWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		map = newMap();
		for(BlockNode node: flowGraph) {
			for(ICode instr: node) {
				HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>> hash = newSet();
				if(instr instanceof Def) {
					Def def = (Def)instr;
					if(def.scope != Scope.GLOBAL && def.isConstant())
						hash.add(new Tuple<>(def, new Tuple<CopyStr, NullableExp>(new CopyStr(def.label), def.val)));
				} else if(instr instanceof Assign) {
					Assign ass = (Assign)instr;
					if(ass.isConstant()) {
						hash.add(new Tuple<>(ass, new Tuple<>(new CopyStr(ass.place), ass.value)));
					}
				} else  if(instr instanceof Call) {
					Call call = (Call)instr;
					for(Def def: call.params){
						if(def.scope != Scope.GLOBAL && def.isConstant())
							hash.add(new Tuple<>(def, new Tuple<CopyStr, NullableExp>(new CopyStr(def.label), def.val)));
					}
				}
				map.put(instr, hash);
			}
		}
	}

	@Override
	public HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>> transferFunction(ICode instr,
			HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>> inputSet) {
		HashSet<Tuple<ICode, Tuple<CopyStr, NullableExp>>> newSet = newSet();
		
		if(instr instanceof BssSec){
			return newSet;
		} else {
			newSet.addAll(map.get(instr));
			newSet.addAll(inputSet);
			return newSet;
		}
	}
}
