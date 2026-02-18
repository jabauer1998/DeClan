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
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.ICode;
import declan.middleware.icode.ICode.Scope;
import declan.middleware.icode.exp.NullableExp;
import declan.middleware.icode.section.BssSec;
import declan.middleware.icode.section.CodeSec;
import declan.middleware.icode.section.DataSec;
import declan.middleware.icode.section.ProcSec;
import declan.utils.Utils;

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
