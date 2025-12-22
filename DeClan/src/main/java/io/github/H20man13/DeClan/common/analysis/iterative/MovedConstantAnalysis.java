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
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Scope;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.section.CodeSec;
import io.github.H20man13.DeClan.common.icode.section.ProcSec;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;

public class MovedConstantAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<CopyStr, CopyStr>>>, HashSet<Tuple<CopyStr, CopyStr>>, Tuple<CopyStr, CopyStr>> {
    private Map<ICode, HashSet<CopyStr>> toRemove;
    private Map<ICode, HashSet<Tuple<CopyStr, CopyStr>>> toAdd;
    
	public MovedConstantAnalysis(Lib prog, FlowGraph flowGraph, HashSet<Tuple<CopyStr, CopyStr>> semilattice, Config cfg) {
		super(flowGraph, Direction.FORWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.toRemove = new HashMap<ICode, HashSet<CopyStr>>();
		this.toAdd = newMap();
		ICode first = prog.getInstruction(0);
		
		for(BlockNode node: flowGraph) {
			for(ICode instr: node) {
				HashSet<CopyStr> toRemove = new HashSet<CopyStr>();
				if(!instr.equals(first)){
					if(instr instanceof Def) {
						Def def = (Def)instr;
						if(def.scope != Scope.GLOBAL && def.isConstant())
							toRemove.add(new CopyStr(def.label));
					} else if(instr instanceof Call) {
						Call call = (Call)instr;
						for(Def def: call.params){
							if(def.isConstant() && def.scope != Scope.GLOBAL)
								toRemove.add(new CopyStr(def.label));
						}
					}
					toAdd.put(instr, new HashSet<>());
					this.toRemove.put(instr, toRemove);
				} else {
					this.toAdd.put(instr, semilattice);
					this.toRemove.put(instr, toRemove);
				}
			}
		}
	}

	@Override
	public HashSet<Tuple<CopyStr, CopyStr>> transferFunction(ICode instr, HashSet<Tuple<CopyStr, CopyStr>> inputSet) {
		HashSet<Tuple<CopyStr, CopyStr>> newSet = newSet();
		
		for(Tuple<CopyStr, CopyStr> str: inputSet){
			if(!toRemove.get(instr).contains(str.source))
				newSet.add(str);
		}
		
		newSet.addAll(toAdd.get(instr));
		
		return newSet;
	}
}
