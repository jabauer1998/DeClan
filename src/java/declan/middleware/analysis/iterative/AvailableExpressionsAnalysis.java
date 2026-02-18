package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import declan.driver.Config;
import declan.utils.Tuple;
import declan.utils.flow.BlockNode;
import declan.utils.flow.FlowGraph;
import declan.utils.flow.FlowGraphNode;
import declan.middleware.icode.Assign;
import declan.middleware.icode.Call;
import declan.middleware.icode.Def;
import declan.middleware.icode.ICode;
import declan.middleware.icode.If;
import declan.middleware.icode.exp.BinExp;
import declan.middleware.icode.exp.Exp;
import declan.middleware.icode.exp.IdentExp;
import declan.middleware.icode.exp.NullableExp;
import declan.middleware.icode.exp.UnExp;
import declan.middleware.icode.inline.Inline;
import declan.middleware.icode.inline.InlineParam;
import declan.utils.Utils;

public class AvailableExpressionsAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<NullableExp, ICode.Type>>>, HashSet<Tuple<NullableExp, ICode.Type>>, Tuple<NullableExp, ICode.Type>> {

    private AnticipatedExpressionsAnalysis anticipatedAnalysis;
    private Map<ICode, Set<String>> killSets;

    public AvailableExpressionsAnalysis(FlowGraph flowGraph, AnticipatedExpressionsAnalysis analysis, HashSet<Tuple<NullableExp, ICode.Type>> globalFlowSet, Config cfg) {
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
    public HashSet<Tuple<NullableExp, ICode.Type>> transferFunction(ICode instruction, HashSet<Tuple<NullableExp, ICode.Type>> inputSet) {
        HashSet<Tuple<NullableExp, ICode.Type>> result = newSet();

        result.addAll(inputSet);
        result.addAll(this.anticipatedAnalysis.getInputSet(instruction));
        
        HashSet<Tuple<NullableExp, ICode.Type>> resultCopy = newSet();
        resultCopy.addAll(result);
        
        for(Tuple<NullableExp, ICode.Type> exp: resultCopy) {
        	for(String s: killSets.get(instruction)) {
        		if(((Exp)exp.source).containsPlace(s)) {
        			result.remove(exp);
        		}
        	}
        }

        return result;
    }
    
}
