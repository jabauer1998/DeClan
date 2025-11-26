package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
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
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.util.Utils;

public class AnticipatedExpressionsAnalysis extends 
InstructionAnalysis<HashMap<ICode, HashSet<Tuple<Exp, ICode.Type>>>, 
HashSet<Tuple<Exp, ICode.Type>>, 
Tuple<Exp, ICode.Type>>{
    private HashMap<ICode, HashSet<Tuple<Exp, ICode.Type>>> genSets;
    private HashMap<ICode, Set<String>> killSets;

    public AnticipatedExpressionsAnalysis(FlowGraph flowGraph, HashSet<Tuple<Exp, ICode.Type>> globalFlowSet, Config cfg) {
        super(flowGraph, Direction.BACKWARDS, Meet.INTERSECTION, globalFlowSet, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
        genSets = newMap();
        killSets =  new HashMap<ICode, Set<String>>();

        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> codeList = block.getICode();
            for(int i = codeList.size() - 1; i >= 0; i--){
                ICode icode = codeList.get(i);
                HashSet<String> instructionKill = new HashSet<String>();
                HashSet<Tuple<Exp, ICode.Type>> instructionGen = newSet();
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
                } else if(icode instanceof Inline) {
                	Inline inICode = (Inline)icode;
                	for(InlineParam param: inICode.params) {
                		if(param.containsAllQual(InlineParam.IS_USE))
                			instructionGen.add(new Tuple<Exp, ICode.Type>(param.name, param.type));
                		else if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			instructionKill.add(param.name.ident);
                	}
                }
                
                genSets.put(icode, instructionGen);
                killSets.put(icode, instructionKill);
            }
        }
    }


    @Override
    public HashSet<Tuple<Exp, ICode.Type>> transferFunction(ICode icode, HashSet<Tuple<Exp, ICode.Type>> inputSet) {
        HashSet<Tuple<Exp, ICode.Type>> result = newSet();
        
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
