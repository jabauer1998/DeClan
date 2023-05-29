package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class ConstantPropogationAnalysis extends Analysis<Tuple<String, Object>> {

    private Map<ICode, Set<Tuple<String, Object>>> constDefinitions;
    private Map<ICode, Set<Tuple<String, Object>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.constDefinitions = new HashMap<ICode, Set<Tuple<String, Object>>>();
        this.killDefinitions = new HashMap<ICode, Set<Tuple<String, Object>>>();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode icode : block.getICode()){
                Set<Tuple<String, Object>> setTuples = new HashSet<Tuple<String, Object>>();
                Set<Tuple<String, Object>> killTuples = new HashSet<Tuple<String, Object>>();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    if(assICode.isConstant()){
                        Object val = Utils.getValue(assICode.value);
                        Tuple<String, Object> newTuple = new Tuple<String, Object>(assICode.place, val);
                        setTuples.add(newTuple);
                    } else {
                        Tuple<String, Object> killTuple = new Tuple<String, Object>(assICode.place, null);
                        killTuples.add(killTuple);
                    }
                }
                constDefinitions.put(icode, setTuples);
                killDefinitions.put(icode, killTuples);
            }
        }
    }

    @Override
    public Set<Tuple<String, Object>> transferFunction(FlowGraphNode block, ICode instruction, Set<Tuple<String, Object>> inputSet){
        Set<Tuple<String, Object>> result = new HashSet<Tuple<String, Object>>();

        result.addAll(inputSet);

        for(Tuple<String, Object> killTuple : killDefinitions.get(instruction)){
            String killText = killTuple.source;
            for(Tuple<String, Object> singleResult : result){
                if(killText.equals(singleResult.source)){
                    result.remove(singleResult);
                }
            }
        }

        result.addAll(constDefinitions.get(instruction));

        Map<String, Set<Tuple<String, Object>>> found = new HashMap<String, Set<Tuple<String, Object>>>();
        for(Tuple<String, Object> setsFound : result){
            if(!found.containsKey(setsFound.source)){
                found.put(setsFound.source, new HashSet<Tuple<String, Object>>());
            }

            Set<Tuple<String, Object>> tuples = found.get(setsFound.source);
            tuples.add(setsFound);
        }

        for(Set<Tuple<String, Object>> setFound : found.values()){
            if(setFound.size() > 1){
                result.removeAll(setFound);
            }
        }
        
        return result;
    }
}
