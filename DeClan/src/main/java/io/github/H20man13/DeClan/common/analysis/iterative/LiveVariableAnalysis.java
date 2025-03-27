package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
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
import io.github.H20man13.DeClan.common.icode.exp.BoolExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class LiveVariableAnalysis extends InstructionAnalysis<String> {

    private Map<ICode, Set<String>> defSets;
    private Map<ICode, Set<String>> useSets;

    public LiveVariableAnalysis(FlowGraph flowGraph) {
        super(flowGraph, Direction.BACKWARDS, Meet.UNION);

        this.defSets = new HashMap<ICode, Set<String>>();
        this.useSets = new HashMap<ICode, Set<String>>();
        
        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode code : block.getICode()){
                Set<String> instructionDef = new HashSet<String>();
                Set<String> instructionUse = new HashSet<String>();
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
                }
                defSets.put(code, instructionDef);
                useSets.put(code, instructionUse);
            }
        }

    }

    @Override
    public Set<String> transferFunction(ICode instruction, Set<String> inputSet) {
        Set<String> resultSet = new HashSet<String>();

        resultSet.addAll(inputSet);
        Set<String> useSet = useSets.get(instruction);
        Set<String> defSet = defSets.get(instruction);
        resultSet.addAll(useSet);
        resultSet.removeAll(defSet);
        resultSet.addAll(useSets.get(instruction));

        return resultSet;
    }
}
