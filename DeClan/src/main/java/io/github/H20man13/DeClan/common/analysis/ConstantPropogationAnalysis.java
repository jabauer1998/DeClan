package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

public class ConstantPropogationAnalysis extends InstructionAnalysis<Tuple<String, Exp>> {

    private Map<ICode, Set<Tuple<String, Exp>>> constDefinitions;
    private Map<ICode, Set<Tuple<String, Exp>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, new Function<List<Set<Tuple<String, Exp>>>, Set<Tuple<String, Exp>>>(){
            @Override
            public Set<Tuple<String, Exp>> apply(List<Set<Tuple<String, Exp>>> list) {
                Set<Tuple<String, Exp>> resultSet = new HashSet<Tuple<String, Exp>>();
                HashMap<String, Set<Exp>> hashMap = new HashMap<String, Set<Exp>>();

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
                    Set<Exp> objValues = hashMap.get(key);
                    if(objValues.size() == 1){
                        for(Exp val :  objValues){
                            resultSet.add(new Tuple<String, Exp>(key, val));
                        }
                    }
                }

                return resultSet;
            }
        });

        this.constDefinitions = new HashMap<ICode, Set<Tuple<String, Exp>>>();
        this.killDefinitions = new HashMap<ICode, Set<Tuple<String, Exp>>>();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                Set<Tuple<String, Exp>> setTuples = new HashSet<Tuple<String, Exp>>();
                Set<Tuple<String, Exp>> killTuples = new HashSet<Tuple<String, Exp>>();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    if(assICode.isConstant()){
                        Tuple<String, Exp> newTuple = new Tuple<String, Exp>(assICode.place, assICode.value);
                        setTuples.add(newTuple);
                    } else if(assICode.value instanceof IdentExp){
                        Tuple<String, Exp> newTuple = new Tuple<String,Exp>(assICode.place, assICode.value);
                        setTuples.add(newTuple);
                    } else {
                        Tuple<String, Exp> killTuple = new Tuple<String, Exp>(assICode.place, null);
                        killTuples.add(killTuple);
                    }
                } else if(icode instanceof Def){
                	Def assICode = (Def)icode;
                    if(assICode.isConstant()){
                        Tuple<String, Exp> newTuple = new Tuple<String, Exp>(assICode.label, assICode.val);
                        setTuples.add(newTuple);
                    } else if(assICode.val instanceof IdentExp){
                        Tuple<String, Exp> newTuple = new Tuple<String,Exp>(assICode.label, assICode.val);
                        setTuples.add(newTuple);
                    } else {
                        Tuple<String, Exp> killTuple = new Tuple<String, Exp>(assICode.label, null);
                        killTuples.add(killTuple);
                    }
                }

                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public Set<Tuple<String, Exp>> transferFunction(ICode instruction, Set<Tuple<String, Exp>> inputSet){
        Set<Tuple<String, Exp>> result = new HashSet<Tuple<String, Exp>>();
        result.addAll(inputSet);

        Set<Tuple<String, Exp>> finalResult = new HashSet<Tuple<String, Exp>>();

        Set<Tuple<String, Exp>> killSet = killDefinitions.get(instruction);
        for(Tuple<String, Exp> res: result){
            String resTest = res.source;
            if(!Utils.containsExpInSet(killSet, resTest)){
                finalResult.add(res);
            }
        }

        finalResult.addAll(constDefinitions.get(instruction));
        
        return finalResult;
    }
}
