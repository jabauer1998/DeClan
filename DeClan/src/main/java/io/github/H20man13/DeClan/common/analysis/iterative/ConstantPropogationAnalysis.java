package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.ConversionUtils;
import io.github.H20man13.DeClan.common.util.OpUtil;
import io.github.H20man13.DeClan.common.util.Utils;

public class ConstantPropogationAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<String, Exp>>>, HashSet<Tuple<String, Exp>>, Tuple<String, Exp>> 
implements CustomMeet<HashSet<Tuple<String, Exp>>, Tuple<String, Exp>>{

    private Map<ICode, HashSet<Tuple<String, Exp>>> constDefinitions;
    private Map<ICode, HashSet<Tuple<String, Exp>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph, Config cfg) {
        super(flowGraph, Direction.FORWARDS, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.constDefinitions = newMap();
        this.killDefinitions = newMap();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                HashSet<Tuple<String, Exp>> setTuples = newSet();
                HashSet<Tuple<String, Exp>> killTuples = newSet();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    if(assICode.isConstant()){
                        Tuple<String, Exp> newTuple = new Tuple<String, Exp>(assICode.place, assICode.value);
                        setTuples.add(newTuple);
                    } else if(assICode.value instanceof IdentExp){
                        Tuple<String, Exp> newTuple = new Tuple<String,Exp>(assICode.place, assICode.value);
                        setTuples.add(newTuple);
                    }
                    Tuple<String, Exp> killTuple = new Tuple<String, Exp>(assICode.place, assICode.value);
                    killTuples.add(killTuple);
                } else if(icode instanceof Def){
                	Def assICode = (Def)icode;
                    if(assICode.isConstant()){
                        Tuple<String, Exp> newTuple = new Tuple<String, Exp>(assICode.label, assICode.val);
                        setTuples.add(newTuple);
                    } else if(assICode.val instanceof IdentExp){
                        Tuple<String, Exp> newTuple = new Tuple<String,Exp>(assICode.label, assICode.val);
                        setTuples.add(newTuple);
                    }
                }

                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public HashSet<Tuple<String, Exp>> transferFunction(ICode instruction, HashSet<Tuple<String, Exp>> inputSet){
        HashSet<Tuple<String, Exp>> result = newSet();
        result.addAll(inputSet);

        HashSet<Tuple<String, Exp>> finalResult = newSet();

        HashSet<Tuple<String, Exp>> killSet = killDefinitions.get(instruction);
        for(Tuple<String, Exp> res: result){
            String resTest = res.source;
            if(!Utils.containsExpInSet(killSet, resTest))
            	if(!Utils.containsDestExpInSet(killSet, resTest))
            		finalResult.add(res);
        }

        finalResult.addAll(constDefinitions.get(instruction));
        
        return finalResult;
    }

	@Override
	public HashSet<Tuple<String, Exp>> performMeet(List<HashSet<Tuple<String, Exp>>> list) {
       HashSet<Tuple<String, Exp>> resultSet = newSet();
       HashMap<String, HashSet<Exp>> hashMap = new HashMap<String, HashSet<Exp>>();

        for(Set<Tuple<String, Exp>> set : list){
            for(Tuple<String, Exp> tup : set){
                String tupName = tup.source;
                if(!hashMap.containsKey(tupName)){
                    hashMap.put(tupName, new HashSet<Exp>());
                }

                Set<Exp> objList = hashMap.get(tupName);
                objList.add(tup.dest);
            }
        }

        for(String key : hashMap.keySet()){
            HashSet<Exp> objValues = hashMap.get(key);
            if(objValues.size() == 1){
                for(Exp val :  objValues){
                    resultSet.add(new Tuple<String, Exp>(key, val));
                }
            }
        }

        return resultSet;
	}
}
