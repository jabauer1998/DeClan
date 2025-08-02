package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
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
import io.github.H20man13.DeClan.common.icode.Lib.SymbolSearchStrategy;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.util.Utils;

public class LiveVariableAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<String>>, HashSet<String>, String> {

    private Map<ICode, HashSet<String>> defSets;
    private Map<ICode, HashSet<String>> useSets;

    @SuppressWarnings("unchecked")
	public LiveVariableAnalysis(Prog orig, FlowGraph flowGraph, Config cfg) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));

        this.defSets = newMap();
        this.useSets = newMap();
        
        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode code : block.getICode()){
                HashSet<String> instructionDef = newSet();
                HashSet<String> instructionUse = newSet();
                if(code instanceof Assign){
                    Assign assCode = (Assign)code;
                    instructionDef.add(assCode.place);
                    if(assCode.value instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.value;
                        
                        instructionUse.add(defPlace.left.ident);
                        instructionUse.add(defPlace.right.ident);
                    } else if(assCode.value instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.value;
                        
                        instructionUse.add(defPlace.right.ident);
                    } else if(assCode.value instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.value;
                        instructionUse.add(defPlace.ident);
                    }
                } else if(code instanceof Def){
                	Def assCode = (Def)code;
                    instructionDef.add(assCode.label);
                    if(assCode.val instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.val;
                        
                        instructionUse.add(defPlace.left.ident);
                        instructionUse.add(defPlace.right.ident);
                    } else if(assCode.val instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.val;
                        
                        instructionUse.add(defPlace.right.ident);
                    } else if(assCode.val instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.val;
                        instructionUse.add(defPlace.ident);
                    }
                } else if(code instanceof If){
                    BinExp exp = ((If)code).exp;
                    
                    instructionUse.add(exp.left.ident);
                    instructionUse.add(exp.right.ident);
                } else if(code instanceof Call){
                    Call placement = (Call)code;
                    
                    if(orig.containsEntry(placement.pname, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME)) {
                    	SymEntry data = orig.getVariableData(placement.pname, SymEntry.INTERNAL | SymEntry.RETURN, SymbolSearchStrategy.FIND_VIA_FUNCTION_NAME);
                    	instructionDef.add(data.icodePlace);
                    }
                    
                    for(Def arg : placement.params){
                        instructionDef.add(arg.label);
                        
                        if(arg.val instanceof IdentExp) {
                        	IdentExp expIdent = (IdentExp)arg.val;
                        	instructionUse.add(expIdent.ident);
                        } else if(arg.val instanceof UnExp){
                        	UnExp unaryExpression = (UnExp)arg.val;
                        	instructionUse.add(unaryExpression.right.ident);
                        } else if(arg.val instanceof BinExp) {
                        	BinExp binaryExpression = (BinExp)arg.val;
                        	instructionUse.add(binaryExpression.left.ident);
                        	instructionUse.add(binaryExpression.right.ident);
                        }
                    }
                } else if(code instanceof Inline) {
                	Inline inline = (Inline)code;
                	
                	for(InlineParam param: inline.params) {
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			instructionDef.add(param.name.ident);
                		else if(param.containsAllQual(InlineParam.IS_USE))
                			instructionUse.add(param.name.ident);
                	}
                }
                defSets.put(code, instructionDef);
                useSets.put(code, instructionUse);
            }
        }

    }

    @Override
    public HashSet<String> transferFunction(ICode instruction, HashSet<String> inputSet) {
        HashSet<String> resultSet = newSet();

        resultSet.addAll(inputSet);
        Set<String> useSet = useSets.get(instruction);
        Set<String> defSet = defSets.get(instruction);
        resultSet.removeAll(defSet);
        resultSet.addAll(useSet);

        return resultSet;
    }
}
