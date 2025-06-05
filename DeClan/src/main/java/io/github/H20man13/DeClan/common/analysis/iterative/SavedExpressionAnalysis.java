package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.NaaExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class SavedExpressionAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<Exp, String>>>, HashSet<Tuple<Exp, String>>, Tuple<Exp, String>>
implements CustomMeet<HashSet<Tuple<Exp, String>>, Tuple<Exp, String>>{
	private Map<ICode, Set<Tuple<Exp, ICode.Type>>> opSet;
	private IrRegisterGenerator gen;
	private Prog prog;
	
	public SavedExpressionAnalysis(Prog program, IrRegisterGenerator gen, FlowGraph flow, Map<ICode, Set<Tuple<Exp, ICode.Type>>> latest, UsedExpressionAnalysis used, Config cfg) {
		super(flow, Direction.FORWARDS, Meet.UNION, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.opSet = new HashMap<ICode, Set<Tuple<Exp, ICode.Type>>>();
		for(ICode icode: latest.keySet()) {
			Set<Tuple<Exp, ICode.Type>> newSet = new HashSet<Tuple<Exp, ICode.Type>>();
			newSet.addAll(latest.get(icode));
			newSet.retainAll(used.getOutputSet(icode));
			opSet.put(icode, newSet);
		}
		this.gen = gen;
		this.prog = prog;
	}


	@Override
	public HashSet<Tuple<Exp, String>> transferFunction(ICode instr, HashSet<Tuple<Exp, String>> inputSet) {
		HashSet<Tuple<Exp, String>> newSet = new HashSet<Tuple<Exp, String>>();
		Set<Tuple<Exp, ICode.Type>> opSet = this.opSet.get(instr);
		for(Tuple<Exp, String> tup: inputSet) {
			if(!Utils.containsExpInSet(opSet, tup.source)){
				newSet.add(tup);
			}
		}
		
		for(Tuple<Exp, ICode.Type> tup: opSet) {
			String next;
			do {
			   next = gen.genNext();	
			} while(prog.containsPlace(next));
			
			newSet.add(new Tuple<Exp, String>(tup.source, next));
		}
		
		return newSet;
	}


	@Override
	public HashSet<Tuple<Exp, String>> performMeet(List<HashSet<Tuple<Exp, String>>> list) {
		   HashSet<Tuple<Exp, String>> resultSet = newSet();
	       HashMap<Exp, HashSet<String>> hashMap = new HashMap<Exp, HashSet<String>>();

	        for(Set<Tuple<Exp, String>> set : list){
	            for(Tuple<Exp, String> tup : set){
	                Exp tupName = tup.source;
	                if(!hashMap.containsKey(tupName)){
	                    hashMap.put(tupName, new HashSet<String>());
	                }

	                Set<String> objList = hashMap.get(tupName);
	                objList.add(tup.dest);
	            }
	        }

	        for(Exp key : hashMap.keySet()){
	            HashSet<String> objValues = hashMap.get(key);
	            if(objValues.size() == 1){
	                for(String val :  objValues){
	                    resultSet.add(new Tuple<Exp, String>(key, val));
	                }
	            }
	        }

	        return resultSet;
	}
}
