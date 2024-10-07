package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.Exp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class AnticipatedExpressionsAnalysis extends InstructionAnalysis<Tuple<Exp, ICode.Type>>{
    private Map<ICode, Set<Tuple<Exp, ICode.Type>>> genSets;
    private Map<ICode, Set<String>> killSets;

    public AnticipatedExpressionsAnalysis(FlowGraph flowGraph, Set<Tuple<Exp, ICode.Type>> globalFlowSet) {
        super(flowGraph, Direction.BACKWARDS, Meet.INTERSECTION, globalFlowSet);
        genSets = new HashMap<ICode, Set<Tuple<Exp, ICode.Type>>>();
        killSets =  new HashMap<ICode, Set<String>>();

        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> codeList = block.getICode();
            for(int i = codeList.size() - 1; i >= 0; i--){
                ICode icode = codeList.get(i);
                Set<String> instructionKill = new HashSet<String>();
                Set<Tuple<Exp, ICode.Type>> instructionGen = new HashSet<Tuple<Exp, ICode.Type>>();
                if(icode instanceof Assign){
                    Assign assIcode = (Assign)icode;
                    instructionGen.add(new Tuple<Exp, ICode.Type>(assIcode.value, assIcode.getType()));
                    instructionKill.add(assIcode.place);
                } else if(icode instanceof Def) {
                	Def definition = (Def)icode;
                	instructionGen.add(new Tuple<Exp, ICode.Type>(definition.val, definition.type));
                	instructionKill.add(definition.label);
                } else if(icode instanceof If) {
                	If icodeIf = (If)icode;
                    instructionGen.add(new Tuple<Exp, ICode.Type>(icodeIf.exp, ICode.Type.BOOL));
                } else if(icode instanceof Call) {
                	Call callICode = (Call)icode;
                	List<Def> params = callICode.params;
                	
                	for(Def param: params) {
                        instructionGen.add(new Tuple<Exp, ICode.Type>(param.val, param.type));
                        instructionKill.add(param.label);
                	}
                }
                
                genSets.put(icode, instructionGen);
                killSets.put(icode, instructionKill);
            }
        }
    }


    @Override
    public Set<Tuple<Exp, ICode.Type>> transferFunction(ICode icode, Set<Tuple<Exp, ICode.Type>> inputSet) {
        Set<Tuple<Exp, ICode.Type>> result = new HashSet<Tuple<Exp, ICode.Type>>();
        
        result.addAll(inputSet);
        for(Tuple<Exp, ICode.Type> exp: inputSet) {
        	for(String toKill: killSets.get(icode)) {
        		if(exp.source.containsPlace(toKill)) {
        			result.remove(exp);
        		}
        	}
        }
        
        result.addAll(genSets.get(icode));

        return result;
    }
    
}
