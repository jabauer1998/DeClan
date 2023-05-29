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
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.Utils;

public class ReachingDefinitionsAnalysis extends Analysis<ICode> {
    private Map<ICode, Set<ICode>> killSets;
    private Map<ICode, Set<ICode>> genSets;

    public ReachingDefinitionsAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.killSets = new HashMap<ICode, Set<ICode>>();
        this.genSets = new HashMap<ICode, Set<ICode>>();
        
        Map<String, List<ICode>> declsDeclared = new HashMap<String, List<ICode>>();
        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> decls = block.getICode();
            for(ICode decl : decls){
                if(decl instanceof Assign){
                    Assign assDecl = (Assign)decl;

                    if(declsDeclared.containsKey(assDecl.place)){
                        declsDeclared.put(assDecl.place, new LinkedList<ICode>());
                    }

                    List<ICode> declList = declsDeclared.get(assDecl.place);
                    declList.add(decl);
                }
            }
        }

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode decl : block.getICode()){
                Set<ICode> instructionGenSet = new HashSet<ICode>();
                Set<ICode> instructionKillSet = new HashSet<ICode>();
                if(decl instanceof Assign){
                    Assign declAssign = (Assign)decl;

                    instructionKillSet.addAll(Utils.stripFromListExcept(declsDeclared.get(declAssign.place), decl));
                    instructionGenSet.add(decl);
                }

                killSets.put(decl, instructionKillSet);
                genSets.put(decl, instructionGenSet);
            }
        }
    }

    @Override
    public Set<ICode> transferFunction(FlowGraphNode block, ICode node, Set<ICode> inputSet){
        Set<ICode> result = new HashSet<>();

        Set<ICode> killSet = killSets.get(node);
        Set<ICode> genSet = genSets.get(node);
        
        result.addAll(inputSet);
        result.removeAll(killSet);
        result.addAll(genSet);

        return result;
    }
    
}
