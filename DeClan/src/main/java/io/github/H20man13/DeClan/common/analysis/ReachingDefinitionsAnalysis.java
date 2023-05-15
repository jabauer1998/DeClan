package io.github.H20man13.DeClan.common.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.Utils;

public class ReachingDefinitionsAnalysis extends Analysis<ICode> {
    private Map<FlowGraphNode, Set<ICode>> killSets;
    private Map<FlowGraphNode, Set<ICode>> genSets;

    public ReachingDefinitionsAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.killSets = new HashMap<FlowGraphNode, Set<ICode>>();
        this.genSets = new HashMap<FlowGraphNode, Set<ICode>>();
        
        Map<String, List<ICode>> declsDeclared = new HashMap<String, List<ICode>>();
        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> decls = block.getICode();
            for(ICode decl : decls){
                String place = Utils.getPlace(decl);

                if(place != null){
                    if(!declsDeclared.containsKey(place)){
                        declsDeclared.put(place, new LinkedList<ICode>());
                    }

                    List<ICode> declList = declsDeclared.get(place);
                    declList.add(decl);
                }
            }
        }

        for(BlockNode block : flowGraph.getBlocks()){
            Set<ICode> blockGenSet = new HashSet<ICode>();
            Set<ICode> blockKillSet = new HashSet<ICode>();
            for(ICode decl : block.getICode()){
                String place = Utils.getPlace(decl);

                if(place != null){
                    blockKillSet.addAll(Utils.stripFromListExcept(declsDeclared.get(place), decl));
                    blockGenSet.add(decl);
                }
            }
            killSets.put(block, blockKillSet);
            genSets.put(block, blockGenSet);
        }
    }

    @Override
    public Set<ICode> transferFunction(FlowGraphNode node, Set<ICode> inputSet){
        Set<ICode> result = new HashSet<>();

        Set<ICode> killSet = killSets.get(node);
        Set<ICode> genSet = genSets.get(node);
        
        result.addAll(inputSet);
        result.removeAll(killSet);
        result.addAll(genSet);

        return result;
    }
    
}
