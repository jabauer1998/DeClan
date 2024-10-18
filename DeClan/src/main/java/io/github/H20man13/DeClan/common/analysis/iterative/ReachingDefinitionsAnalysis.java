package io.github.H20man13.DeClan.common.analysis.iterative;

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
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.util.Utils;

public class ReachingDefinitionsAnalysis extends InstructionAnalysis<String> {
    private Map<ICode, Set<String>> killSets;
    private Map<ICode, Set<String>> genSets;

    public ReachingDefinitionsAnalysis(FlowGraph flowGraph, LiveVariableAnalysis liveAnal) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION);

        this.killSets = new HashMap<ICode, Set<String>>();
        this.genSets = new HashMap<ICode, Set<String>>();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode decl : block.getICode()){
            	Set<String> instructionGenSet = new HashSet<String>();
                Set<String> instructionKillSet = new HashSet<String>();
                
                if(decl instanceof Assign){
                    Assign declAssign = (Assign)decl;

                    instructionKillSet.add(declAssign.place);
                    
                    if(!liveAnal.getOutputSet(decl).contains(declAssign.place)) {
                    	instructionGenSet.add(declAssign.place);
                    }
                } else if(decl instanceof Def) {
                	Def declDef = (Def)decl;
                	
                	if(!liveAnal.getOutputSet(decl).contains(declDef.label)) {
                		instructionGenSet.add(declDef.label);
                	}
                }
                
                killSets.put(decl, instructionKillSet);
                genSets.put(decl, instructionGenSet);
            }
        }
    }

    @Override
    public Set<String> transferFunction(ICode block, Set<String> inputSet){
        Set<String> result = new HashSet<String>();

        Set<String> killSet = killSets.get(block);
        Set<String> genSet = genSets.get(block);
        
        result.addAll(inputSet);
        result.removeAll(killSet);
        result.addAll(genSet);

        return result;
    }
    
}
