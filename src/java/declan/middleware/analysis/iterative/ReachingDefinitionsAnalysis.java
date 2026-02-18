package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import declan.driver.Config;
import declan.utils.flow.BlockNode;
import declan.utils.flow.FlowGraph;
import declan.utils.flow.FlowGraphNode;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Def;
import declan.middleware.icode.ICode;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.inline.Inline;
import declan.middleware.icode.inline.InlineParam;
import declan.utils.Utils;

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

                    if(liveAnal.getOutputSet(decl).contains(declAssign.place)) {
                    	instructionKillSet.add(declAssign.place);
                    }
                } else if(decl instanceof Def) {
                	Def declDef = (Def)decl;
                	
                	if(!liveAnal.getOutputSet(decl).contains(declDef.label)){
                		instructionGenSet.add(declDef.label);
                	}
                } else if(decl instanceof Inline) {
                	Inline inline = (Inline)decl;
                	
                	for(InlineParam param: inline.params) {
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			if(!liveAnal.getOutputSet(decl).contains(param.name.ident)) {
                            	instructionKillSet.add(param.name.ident);
                            }
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
