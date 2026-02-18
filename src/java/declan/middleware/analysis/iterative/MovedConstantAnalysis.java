package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import declan.driver.Config;
import declan.utils.CopyStr;
import declan.utils.Tuple;
import declan.middleware.analysis.AnalysisBase.Direction;
import declan.middleware.analysis.AnalysisBase.Meet;
import declan.utils.flow.BlockNode;
import declan.utils.flow.FlowGraph;
import declan.frontend.IrRegisterGenerator;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.ICode;
import declan.middleware.icode.ICode.Scope;
import declan.middleware.icode.Lib;
import declan.middleware.icode.Prog;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.NullableExp;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.ProcSec;
import declan.utils.ConversionUtils;
import declan.utils.Utils;

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
