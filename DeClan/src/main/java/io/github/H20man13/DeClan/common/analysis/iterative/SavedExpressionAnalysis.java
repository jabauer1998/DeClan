package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.gen.IrRegisterGenerator;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.NaaExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.Utils;

public class SavedExpressionAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<NullableExp, CopyStr>>>, HashSet<Tuple<NullableExp, CopyStr>>, Tuple<NullableExp, CopyStr>>
implements CustomMeet<HashSet<Tuple<NullableExp, CopyStr>>>{
	private Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> opSet;
	private IrRegisterGenerator gen;
	private Prog prog;
	private char startLetter;
	private int startNumber;
	
	public SavedExpressionAnalysis(Prog program, IrRegisterGenerator gen, FlowGraph flow, Map<ICode, Set<Tuple<NullableExp, ICode.Type>>> latest, UsedExpressionAnalysis used, Config cfg) {
		super(flow, Direction.FORWARDS, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.opSet = new HashMap<ICode, Set<Tuple<NullableExp, ICode.Type>>>();
		for(ICode icode: latest.keySet()) {
			Set<Tuple<NullableExp, ICode.Type>> newSet = new HashSet<Tuple<NullableExp, ICode.Type>>();
			Set<Tuple<NullableExp, ICode.Type>> latestSet = latest.get(icode);
			Set<Tuple<NullableExp, ICode.Type>> usedSet = used.getOutputSet(icode);
			newSet.addAll(latestSet);
			newSet.retainAll(usedSet);
			opSet.put(icode, newSet);
		}
		this.gen = gen;
		this.prog = program;
		this.startLetter = gen.getCurrentLetter();
		this.startNumber = gen.getCurrentNumber();
	}
	
	@Override
	protected void analysisLoopEndAction() {
		this.gen.synch(startLetter, startNumber);
	}

	@Override
	public HashSet<Tuple<NullableExp, CopyStr>> transferFunction(ICode instr, HashSet<Tuple<NullableExp, CopyStr>> inputSet) {
		HashSet<Tuple<NullableExp, CopyStr>> newSet = new HashSet<Tuple<NullableExp, CopyStr>>();
		Set<Tuple<NullableExp, ICode.Type>> opSet = this.opSet.get(instr);
		for(Tuple<NullableExp, CopyStr> tup: inputSet) {
			if(!Utils.containsExpInSet(opSet, tup.source.toString())){
				newSet.add(tup);
			}
		}
		
		for(Tuple<NullableExp, ICode.Type> tup: opSet) {
			String next;
			do {
			   next = gen.genNext();	
			} while(prog.containsPlace(next));
			
			newSet.add(new Tuple<NullableExp, CopyStr>(tup.source, ConversionUtils.newS(next)));
		}
		
		return newSet;
	}


	@Override
	public HashSet<Tuple<NullableExp, CopyStr>> performMeet(List<HashSet<Tuple<NullableExp, CopyStr>>> list) {
		   HashSet<Tuple<NullableExp, CopyStr>> resultSet = newSet();
	       HashMap<NullableExp, HashSet<String>> hashMap = new HashMap<NullableExp, HashSet<String>>();

	        for(Set<Tuple<NullableExp, CopyStr>> set : list){
	            for(Tuple<NullableExp, CopyStr> tup : set){
	                NullableExp tupName = tup.source;
	                if(!hashMap.containsKey(tupName)){
	                    hashMap.put(tupName, new HashSet<String>());
	                }

	                Set<String> objList = hashMap.get(tupName);
	                objList.add(tup.dest.toString());
	            }
	        }

	        for(NullableExp key : hashMap.keySet()){
	            HashSet<String> objValues = hashMap.get(key);
	            if(objValues.size() == 1){
	                for(String val :  objValues){
	                    resultSet.add(new Tuple<NullableExp, CopyStr>(key, ConversionUtils.newS(val)));
	                }
	            }
	        }

	        return resultSet;
	}
}
