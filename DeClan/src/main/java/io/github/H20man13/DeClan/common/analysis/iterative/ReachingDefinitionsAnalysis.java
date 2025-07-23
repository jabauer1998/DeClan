package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
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

public class ReachingDefinitionsAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<String>>, HashSet<String>, String> {
    private Map<ICode, HashSet<String>> killSets;
    private Map<ICode, HashSet<String>> genSets;

    @SuppressWarnings("unchecked")
	public ReachingDefinitionsAnalysis(FlowGraph flowGraph, LiveVariableAnalysis liveAnal, Config cfg) {
        super(flowGraph, Direction.FORWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.killSets = newMap();
        this.genSets = newMap();

        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode decl : block.getICode()){
            	HashSet<String> instructionGenSet = newSet();
                HashSet<String> instructionKillSet = newSet();
                
                if(decl instanceof Assign){
                    Assign declAssign = (Assign)decl;

                    instructionKillSet.add(declAssign.place);
                    
                    if(liveAnal.getOutputSet(decl).contains(declAssign.place)){
                		instructionGenSet.add(declAssign.place);
                	}
                } else if(decl instanceof Def) {
                	Def declDef = (Def)decl;
                	
                	if(liveAnal.getOutputSet(decl).contains(declDef.label)){
                		instructionGenSet.add(declDef.label);
                	}
                }
                
                killSets.put(decl, instructionKillSet);
                genSets.put(decl, instructionGenSet);
            }
        }
    }

    @Override
    public HashSet<String> transferFunction(ICode block, HashSet<String> inputSet){
        HashSet<String> result = newSet();

        HashSet<String> killSet = killSets.get(block);
        HashSet<String> genSet = genSets.get(block);
        
        result.addAll(inputSet);
        result.removeAll(killSet);
        result.addAll(genSet);

        return result;
    }
    
}
