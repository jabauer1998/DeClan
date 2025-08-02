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

public class AvailableExpressionsAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<Exp, ICode.Type>>>, HashSet<Tuple<Exp, ICode.Type>>, Tuple<Exp, ICode.Type>> {

    private AnticipatedExpressionsAnalysis anticipatedAnalysis;
    private Map<ICode, Set<String>> killSets;

    public AvailableExpressionsAnalysis(FlowGraph flowGraph, AnticipatedExpressionsAnalysis analysis, Set<Tuple<Exp, ICode.Type>> globalFlowSet, Config cfg) {
        super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, globalFlowSet, true, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
        killSets = new HashMap<ICode, Set<String>>();
        this.anticipatedAnalysis = analysis;
        for(BlockNode block : flowGraph.getBlocks()){
            List<ICode> codeList = block.getICode();
            for(int i = 0; i < codeList.size(); i++){
                ICode icode = codeList.get(i);
                Set<String> instructionKill = new HashSet<String>();
                if(icode instanceof Assign){
                    Assign assICode = (Assign)icode;
                    instructionKill.add(assICode.place);
                } else if(icode instanceof Def) {
                	Def definition = (Def)icode;
                	instructionKill.add(definition.label);
                } else if(icode instanceof Call){
                	Call myCall = (Call)icode;
                	
                	for(Def param: myCall.params) {
                		instructionKill.add(param.label);
                	}
                } else if(icode instanceof Inline) {
                	Inline inline = (Inline)icode;
                	for(InlineParam param: inline.params)
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			instructionKill.add(param.name.ident);
                }
                killSets.put(icode, instructionKill);
            }
        }
    }

    @Override
    public HashSet<Tuple<Exp, ICode.Type>> transferFunction(ICode instruction, HashSet<Tuple<Exp, ICode.Type>> inputSet) {
        HashSet<Tuple<Exp, ICode.Type>> result = newSet();

        result.addAll(inputSet);
        result.addAll(this.anticipatedAnalysis.getInputSet(instruction));
        
        HashSet<Tuple<Exp, ICode.Type>> resultCopy = newSet();
        resultCopy.addAll(result);
        
        for(Tuple<Exp, ICode.Type> exp: resultCopy) {
        	for(String s: killSets.get(instruction)) {
        		if(exp.source.containsPlace(s)) {
        			result.remove(exp);
        		}
        	}
        }

        return result;
    }
    
}
