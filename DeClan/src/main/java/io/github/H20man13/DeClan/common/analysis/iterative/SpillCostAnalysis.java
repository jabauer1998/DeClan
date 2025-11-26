package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Config;
import io.github.H20man13.DeClan.common.CustomMeet;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.flow.BlockNode;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Call;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.If;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Spill;
import io.github.H20man13.DeClan.common.icode.Lib.SymbolSearchStrategy;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.NullableExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.icode.inline.InlineParam;
import io.github.H20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.H20man13.DeClan.common.util.Utils;

public class SpillCostAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<String, Integer>>>, HashSet<Tuple<String, Integer>>, Tuple<String, Integer>>
implements CustomMeet<HashSet<Tuple<String, Integer>>>{
	private HashMap<ICode, HashSet<String>> defSets;
	private HashMap<ICode, HashSet<Tuple<String, Integer>>> useSets;
	private Lib orig;
	
	public SpillCostAnalysis(Lib orig, FlowGraph flowGraph, Config cfg) {
		super(flowGraph, Direction.BACKWARDS, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.defSets = new HashMap<ICode, HashSet<String>>();
        this.useSets = newMap();
        this.orig = orig;
        
        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode code : block.getICode()){
                HashSet<String> instructionDef = new HashSet<String>();
                HashSet<Tuple<String, Integer>> instructionUse = newSet();
                if(code instanceof Assign){
                    Assign assCode = (Assign)code;
                    instructionDef.add(assCode.place);
                    if(assCode.value instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.value;
                        
                        instructionUse.add(new Tuple<>(defPlace.left.ident, 0));
                        instructionUse.add(new Tuple<>(defPlace.right.ident, 0));
                    } else if(assCode.value instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.value;
                        
                        instructionUse.add(new Tuple<>(defPlace.right.ident, 0));
                    } else if(assCode.value instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.value;
                        instructionUse.add(new Tuple<>(defPlace.ident, 0));
                    }
                } else if(code instanceof Def){
                	Def assCode = (Def)code;
                    instructionDef.add(assCode.label);
                    if(assCode.val instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.val;
                        
                        instructionUse.add(new Tuple<>(defPlace.left.ident, 0));
                        instructionUse.add(new Tuple<>(defPlace.right.ident, 0));
                    } else if(assCode.val instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.val;
                        
                        instructionUse.add(new Tuple<>(defPlace.right.ident, 0));
                    } else if(assCode.val instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.val;
                        instructionUse.add(new Tuple<>(defPlace.ident, 0));
                    }
                } else if(code instanceof If){
                    BinExp exp = ((If)code).exp;
                    
                    instructionUse.add(new Tuple<>(exp.left.ident, 0));
                    instructionUse.add(new Tuple<>(exp.right.ident, 0));
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
                        	instructionUse.add(new Tuple<>(expIdent.ident, 0));
                        } else if(arg.val instanceof UnExp){
                        	UnExp unaryExpression = (UnExp)arg.val;
                        	instructionUse.add(new Tuple<>(unaryExpression.right.ident, 0));
                        } else if(arg.val instanceof BinExp) {
                        	BinExp binaryExpression = (BinExp)arg.val;
                        	instructionUse.add(new Tuple<>(binaryExpression.left.ident, 0));
                        	instructionUse.add(new Tuple<>(binaryExpression.right.ident, 0));
                        }
                    }
                } else if(code instanceof Inline) {
                	Inline inline = (Inline)code;
                	
                	for(InlineParam param: inline.params) {
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			instructionDef.add(param.name.ident);
                		else if(param.containsAllQual(InlineParam.IS_USE))
                			instructionUse.add(new Tuple<>(param.name.ident, 0));
                	}
                } else if(code instanceof Spill) {
                	Spill mySpill = (Spill)code;
                	instructionDef.add(mySpill.name);
                }
                defSets.put(code, instructionDef);
                useSets.put(code, instructionUse);
            }
        }
	}
	
	private static boolean setContainsLabel(HashSet<Tuple<String, Integer>> set, String label) {
		for(Tuple<String, Integer> elem: set)
			if(elem.source.equals(label))
				return true;
		return false;
	}
	
	private static Tuple<String, Integer> getLabel(HashSet<Tuple<String, Integer>> set, String label) {
		for(Tuple<String, Integer> elem: set)
			if(elem.source.equals(label))
				return elem;
		return null;
	}

	@Override
	public HashSet<Tuple<String, Integer>> transferFunction(ICode instr, HashSet<Tuple<String, Integer>> inputSet) {
		HashSet<Tuple<String, Integer>> use = useSets.get(instr);
		HashSet<String> def = defSets.get(instr);
		HashSet<Tuple<String, Integer>> newSet = newSet();
		
		for(Tuple<String, Integer> in: inputSet)
			if(!def.contains(in.source))
				newSet.add(in);
		
		HashSet<Tuple<String, Integer>> newish = newSet();
		for(Tuple<String, Integer> newElem: newSet)
			if(!setContainsLabel(use, newElem.source))
				newish.add(getLabel(use, newElem.source));
		
		newish.addAll(use);
		for(Tuple<String, Integer> tup: newish){
			tup.dest = tup.dest + 1;
		}
		
		return newish;
	}
	
	@Override
	public HashSet<Tuple<String, Integer>> performMeet(List<HashSet<Tuple<String, Integer>>> list) {
		HashSet<Tuple<String, Integer>> hash = newSet();
		
		for(HashSet<Tuple<String, Integer>> elem: list) {
			for(Tuple<String, Integer> myElem: elem)
				if(setContainsLabel(hash, myElem.source)) {
					Tuple<String, Integer> source = getLabel(hash, myElem.source);
					source.dest = (source.dest + myElem.dest) / 2;
				} else {
					hash.add(new Tuple<>(myElem.source, myElem.dest));
				}
		}
		
		return hash;
	}
}
