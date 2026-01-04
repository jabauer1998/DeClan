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
import io.github.H20man13.DeClan.common.icode.Assign;
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

public class MovedConstantAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>>>, HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>>, Tuple<ICode, Tuple<CopyStr, CopyStr>>> {
    private Map<ICode, HashSet<ICode>> toRemove;
    private Map<ICode, HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>>> toAdd;
    
	public MovedConstantAnalysis(Lib prog, FlowGraph flowGraph, HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>> semilattice, Config cfg) {
		super(flowGraph, Direction.FORWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.toRemove = new HashMap<ICode, HashSet<ICode>>();
		this.toAdd = newMap();
		ICode first = prog.getInstruction(0);
		
		for(BlockNode node: flowGraph) {
			for(ICode instr: node) {
				HashSet<ICode> toRemove = new HashSet<ICode>();
				if(!instr.equals(first)){
					if(instr instanceof Def) {
						Def def = (Def)instr;
						if(def.scope != Scope.GLOBAL && def.isConstant())
							toRemove.add(def);
					} else if(instr instanceof Assign) {
						Assign ass = (Assign)instr;
						if(ass.isConstant())
							toRemove.add(ass);
					} else if(instr instanceof Call) {
						Call call = (Call)instr;
						for(Def def: call.params){
							if(def.isConstant() && def.scope != Scope.GLOBAL)
								toRemove.add(def);
						}
					}
					this.toAdd.put(instr, new HashSet<>());
					this.toRemove.put(instr, toRemove);
				} else {
					this.toAdd.put(instr, semilattice);
					this.toRemove.put(instr, toRemove);
				}
			}
		}
	}

	@Override
	public HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>> transferFunction(ICode instr, HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>> inputSet) {
		HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>> newSet = newSet();
		
		HashSet<ICode> toRem = toRemove.get(instr);
		HashSet<Tuple<ICode, Tuple<CopyStr, CopyStr>>> toAdd = this.toAdd.get(instr);
		
		for(Tuple<ICode, Tuple<CopyStr, CopyStr>> str: inputSet){
			if(!toRem.contains(str.source))
				newSet.add(str);
		}
		
		newSet.addAll(toAdd);
		
		return newSet;
	}
}
