package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.NaaExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class ConstantPropogationAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<String, NullableExp>>>, HashSet<Tuple<String, NullableExp>>, Tuple<String, NullableExp>> 
implements CustomMeet<HashSet<Tuple<String, NullableExp>>, Tuple<String, NullableExp>>{

    private Map<ICode, HashSet<Tuple<String, NullableExp>>> constDefinitions;
    private Map<ICode, HashSet<Tuple<String, NullableExp>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph, Config cfg) {
        super(flowGraph, Direction.FORWARDS, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.constDefinitions = newMap();
        this.killDefinitions = newMap();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                HashSet<Tuple<String, NullableExp>> setTuples = newSet();
                HashSet<Tuple<String, NullableExp>> killTuples = newSet();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    if(assICode.isConstant()){
                        Tuple<String, NullableExp> newTuple = new Tuple<String, NullableExp>(assICode.place, assICode.value);
                        setTuples.add(newTuple);
                    } else if(assICode.value instanceof IdentExp){
                    	IdentExp val = (IdentExp)assICode.value;
                    	if(val.scope != ICode.Scope.RETURN) {
                    		Tuple<String, NullableExp> newTuple = new Tuple<String, NullableExp>(assICode.place, assICode.value);
                    		setTuples.add(newTuple);
                    	}
                    }
                    Tuple<String, NullableExp> killTuple = new Tuple<String, NullableExp>(assICode.place, assICode.value);
                    killTuples.add(killTuple);
                } else if(icode instanceof Def){
                	Def assICode = (Def)icode;
                    if(assICode.isConstant()){
                        Tuple<String, NullableExp> newTuple = new Tuple<String, NullableExp>(assICode.label, assICode.val);
                        setTuples.add(newTuple);
                    } else if(assICode.val instanceof IdentExp){
                    	IdentExp ident = (IdentExp)assICode.val;
                    	if(ident.scope != ICode.Scope.RETURN) {
                    		Tuple<String, NullableExp> newTuple = new Tuple<String, NullableExp>(assICode.label, assICode.val);
                    		setTuples.add(newTuple);
                    	}
                    }
                }

                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public HashSet<Tuple<String, NullableExp>> transferFunction(ICode instruction, HashSet<Tuple<String, NullableExp>> inputSet){
        HashSet<Tuple<String, NullableExp>> result = newSet();
        result.addAll(inputSet);

        HashSet<Tuple<String, NullableExp>> finalResult = newSet();

        HashSet<Tuple<String, NullableExp>> killSet = killDefinitions.get(instruction);
        for(Tuple<String, NullableExp> res: result){
            String resTest = res.source;
            NullableExp resDest = res.dest;            
            if(!Utils.containsExpInSet(killSet, resTest))
            	if(!Utils.containsExpInSet(killSet, resDest))
            		finalResult.add(res);
        }

        finalResult.addAll(constDefinitions.get(instruction));
        
        return finalResult;
    }

	@Override
	public HashSet<Tuple<String, NullableExp>> performMeet(List<HashSet<Tuple<String, NullableExp>>> list) {
       HashSet<Tuple<String, NullableExp>> resultSet = newSet();
       HashMap<String, HashSet<NullableExp>> hashMap = new HashMap<String, HashSet<NullableExp>>();

        for(Set<Tuple<String, NullableExp>> set : list){
            for(Tuple<String, NullableExp> tup : set){
                String tupName = tup.source;
                if(!hashMap.containsKey(tupName)){
                    hashMap.put(tupName, new HashSet<NullableExp>());
                }

                Set<NullableExp> objList = hashMap.get(tupName);
                objList.add(tup.dest);
            }
        }

        for(String key : hashMap.keySet()){
            HashSet<NullableExp> objValues = hashMap.get(key);
            if(objValues.size() == 1){
                for(NullableExp val :  objValues){
                    resultSet.add(new Tuple<String, NullableExp>(key, val));
                }
            } else if(objValues.size() > 1) {
            	resultSet.add(new Tuple<String, NullableExp>(key, new NaaExp()));
            }
        }

        return resultSet;
	}
}
