package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.LetBin;
import io.github.H20man13.DeClan.common.icode.LetBool;
import io.github.H20man13.DeClan.common.icode.LetInt;
import io.github.H20man13.DeClan.common.icode.LetReal;
import io.github.H20man13.DeClan.common.icode.LetString;
import io.github.H20man13.DeClan.common.icode.LetUn;
import io.github.H20man13.DeClan.common.icode.LetVar;

public class ConstantPropogationAnalysis extends Analysis<Tuple<String, Object>> {

    private Map<FlowGraphNode, Set<Tuple<String, Object>>> constDefinitions;
    private Map<FlowGraphNode, Set<Tuple<String, Object>>> killDefinitions;

    public ConstantPropogationAnalysis(FlowGraph flowGraph, Direction direction, Meet symbol) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.constDefinitions = new HashMap<FlowGraphNode, Set<Tuple<String, Object>>>();
        this.killDefinitions = new HashMap<FlowGraphNode, Set<Tuple<String, Object>>>();

        for(BlockNode block : flowGraph.getBlocks()){
            Set<Tuple<String, Object>> setTuples = new HashSet<Tuple<String, Object>>();
            Set<Tuple<String, Object>> killTuples = new HashSet<Tuple<String, Object>>();
            for(ICode icode : block.getICode()){
                if(icode instanceof LetBool){
                    LetBool boolICode = (LetBool)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(boolICode.place, boolICode.value);
                    setTuples.add(newTuple);
                } else if(icode instanceof LetInt){
                    LetInt intICode = (LetInt)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(intICode.place, intICode.value);
                    setTuples.add(newTuple);
                } else if(icode instanceof LetReal){
                    LetReal realICode = (LetReal)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(realICode.place, realICode.value);
                    setTuples.add(newTuple);
                } else if(icode instanceof LetString){
                    LetString strICode = (LetString)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(strICode.place, strICode.value);
                    setTuples.add(newTuple);
                } else if(icode instanceof LetVar){
                    LetVar varICode = (LetVar)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(varICode.place, null);
                    killTuples.add(newTuple);
                } else if(icode instanceof LetUn){
                    LetUn unICode = (LetUn)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(unICode.place, null);
                    killTuples.add(newTuple);
                } else if(icode instanceof LetBin){
                    LetBin binICode = (LetBin)icode;
                    Tuple<String, Object> newTuple = new Tuple<String, Object>(binICode.place, null);
                    killTuples.add(newTuple);
                }
            }
            constDefinitions.put(block, setTuples);
            killDefinitions.put(block, killTuples);
        }
    }

    @Override
    public Set<Tuple<String, Object>> transferFunction(FlowGraphNode Node, Set<Tuple<String, Object>> inputSet){
        Set<Tuple<String, Object>> result = new HashSet<Tuple<String, Object>>();

        result.addAll(inputSet);
        result.addAll(constDefinitions.get(Node));
        
        for(Tuple<String, Object> killTuple : killDefinitions.get(Node)){
            String killText = killTuple.source;
            for(Tuple<String, Object> singleResult : result){
                if(killText.equals(singleResult.source)){
                    result.remove(singleResult);
                }
            }
        }

        Map<String, Set<Tuple<String, Object>>> found = new HashMap<String, Set<Tuple<String, Object>>>();
        for(Tuple<String, Object> setsFound : result){
            if(!found.containsKey(setsFound.source)){
                found.put(setsFound.source, new HashSet<Tuple<String, Object>>());
            }

            Set<Tuple<String, Object>> 
        }
        
        return result;
    }
}
