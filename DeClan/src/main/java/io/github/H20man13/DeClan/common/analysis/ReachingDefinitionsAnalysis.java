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
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.util.Utils;

public class ReachingDefinitionsAnalysis extends BasicBlockAnalysis<ICode> {
    private Map<FlowGraphNode, Set<ICode>> killSets;
    private Map<FlowGraphNode, Set<ICode>> genSets;

    public ReachingDefinitionsAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.killSets = new HashMap<FlowGraphNode, Set<ICode>>();
        this.genSets = new HashMap<FlowGraphNode, Set<ICode>>();
        
        Map<String, List<ICode>> declsDeclared = new HashMap<String, List<ICode>>();
        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> decls = block.getAllICode();
            for(ICode decl : decls){
                if(decl instanceof Assign){
                    Assign assDecl = (Assign)decl;

                    if(declsDeclared.containsKey(assDecl.place)){
                        declsDeclared.put(assDecl.place, new LinkedList<ICode>());
                    }

                    List<ICode> declList = declsDeclared.get(assDecl.place);
                    declList.add(decl);
                } else if(decl instanceof Def) {
                	Def defDecl = (Def)decl;
                	
                	if(declsDeclared.containsKey(defDecl.label)) {
                		declsDeclared.put(defDecl.label, new LinkedList<ICode>());
                	}
                	
                	List<ICode> declList = declsDeclared.get(defDecl.label);
                	declList.add(decl);
                }
            }
        }

        for(BlockNode block : flowGraph.getBlocks()){
        	Set<ICode> instructionGenSet = new HashSet<ICode>();
            Set<ICode> instructionKillSet = new HashSet<ICode>();
            
            for(ICode decl : block.getICode()){
                if(decl instanceof Assign){
                    Assign declAssign = (Assign)decl;

                    instructionKillSet.addAll(Utils.stripFromListExcept(declsDeclared.get(declAssign.place), decl));
                    instructionGenSet.add(decl);
                } else if(decl instanceof Def) {
                	Def declDef = (Def)decl;
                	
                	instructionKillSet.addAll(Utils.stripFromListExcept(declsDeclared.get(declDef.label), decl));
                    instructionGenSet.add(decl);
                }
            }
            
            killSets.put(block, instructionKillSet);
            genSets.put(block, instructionGenSet);
        }
    }

    @Override
    public Set<ICode> transferFunction(FlowGraphNode block, Set<ICode> inputSet){
        Set<ICode> result = new HashSet<>();

        Set<ICode> killSet = killSets.get(block);
        Set<ICode> genSet = genSets.get(block);
        
        result.addAll(inputSet);
        result.removeAll(killSet);
        result.addAll(genSet);

        return result;
    }
    
}
