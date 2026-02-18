package io.github.h20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.h20man13.DeClan.common.Config;
import io.github.h20man13.DeClan.common.CopyInt;
import io.github.h20man13.DeClan.common.CopyStr;
import io.github.h20man13.DeClan.common.CustomMeet;
import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.flow.BlockNode;
import io.github.h20man13.DeClan.common.flow.FlowGraph;
import io.github.h20man13.DeClan.common.flow.FlowGraphNode;
import io.github.h20man13.DeClan.common.icode.Assign;
import io.github.h20man13.DeClan.common.icode.Call;
import io.github.h20man13.DeClan.common.icode.Def;
import io.github.h20man13.DeClan.common.icode.ICode;
import io.github.h20man13.DeClan.common.icode.If;
import io.github.h20man13.DeClan.common.icode.Lib;
import io.github.h20man13.DeClan.common.icode.Spill;
import io.github.h20man13.DeClan.common.icode.Lib.SymbolSearchStrategy;
import io.github.h20man13.DeClan.common.icode.exp.BinExp;
import io.github.h20man13.DeClan.common.icode.exp.IdentExp;
import io.github.h20man13.DeClan.common.icode.exp.NullableExp;
import io.github.h20man13.DeClan.common.icode.exp.UnExp;
import io.github.h20man13.DeClan.common.icode.inline.Inline;
import io.github.h20man13.DeClan.common.icode.inline.InlineParam;
import io.github.h20man13.DeClan.common.icode.symbols.SymEntry;
import io.github.h20man13.DeClan.common.util.ConversionUtils;
import io.github.h20man13.DeClan.common.util.Utils;

public class SpillCostAnalysis extends InstructionAnalysis<HashMap<ICode, HashSet<Tuple<CopyStr, CopyInt>>>, HashSet<Tuple<CopyStr, CopyInt>>, Tuple<CopyStr, CopyInt>>
implements CustomMeet<HashSet<Tuple<CopyStr, CopyInt>>>{
	private HashMap<ICode, HashSet<String>> defSets;
	private HashMap<ICode, HashSet<Tuple<CopyStr, CopyInt>>> useSets;
	private Lib orig;
	
	public SpillCostAnalysis(Lib orig, FlowGraph flowGraph, Config cfg) {
		super(flowGraph, Direction.BACKWARDS, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
		this.defSets = new HashMap<ICode, HashSet<String>>();
        this.useSets = newMap();
        this.orig = orig;
        
        for(BlockNode block : flowGraph.getBlocks()){
            for(ICode code : block.getICode()){
                HashSet<String> instructionDef = new HashSet<String>();
                HashSet<Tuple<CopyStr, CopyInt>> instructionUse = newSet();
                if(code instanceof Assign){
                    Assign assCode = (Assign)code;
                    instructionDef.add(assCode.place);
                    if(assCode.value instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.value;
                        
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.left.ident), ConversionUtils.newI(0)));
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.right.ident), ConversionUtils.newI(0)));
                    } else if(assCode.value instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.value;
                        
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.right.ident), ConversionUtils.newI(0)));
                    } else if(assCode.value instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.value;
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.ident), ConversionUtils.newI(0)));
                    }
                } else if(code instanceof Def){
                	Def assCode = (Def)code;
                    instructionDef.add(assCode.label);
                    if(assCode.val instanceof BinExp){
                        BinExp defPlace = (BinExp)assCode.val;
                        
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.left.ident), ConversionUtils.newI(0)));
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.right.ident), ConversionUtils.newI(0)));
                    } else if(assCode.val instanceof UnExp){
                        UnExp defPlace = (UnExp)assCode.val;
                        
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.right.ident), ConversionUtils.newI(0)));
                    } else if(assCode.val instanceof IdentExp){
                        IdentExp defPlace = (IdentExp)assCode.val;
                        instructionUse.add(new Tuple<>(ConversionUtils.newS(defPlace.ident), ConversionUtils.newI(0)));
                    }
                } else if(code instanceof If){
                    BinExp exp = ((If)code).exp;
                    
                    instructionUse.add(new Tuple<>(ConversionUtils.newS(exp.left.ident), ConversionUtils.newI(0)));
                    instructionUse.add(new Tuple<>(ConversionUtils.newS(exp.right.ident), ConversionUtils.newI(0)));
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
                        	instructionUse.add(new Tuple<>(ConversionUtils.newS(expIdent.ident), ConversionUtils.newI(0)));
                        } else if(arg.val instanceof UnExp){
                        	UnExp unaryExpression = (UnExp)arg.val;
                        	instructionUse.add(new Tuple<>(ConversionUtils.newS(unaryExpression.right.ident), ConversionUtils.newI(0)));
                        } else if(arg.val instanceof BinExp) {
                        	BinExp binaryExpression = (BinExp)arg.val;
                        	instructionUse.add(new Tuple<>(ConversionUtils.newS(binaryExpression.left.ident), ConversionUtils.newI(0)));
                        	instructionUse.add(new Tuple<>(ConversionUtils.newS(binaryExpression.right.ident), ConversionUtils.newI(0)));
                        }
                    }
                } else if(code instanceof Inline) {
                	Inline inline = (Inline)code;
                	
                	for(InlineParam param: inline.params) {
                		if(param.containsAllQual(InlineParam.IS_DEFINITION))
                			instructionDef.add(param.name.ident);
                		else if(param.containsAllQual(InlineParam.IS_USE))
                			instructionUse.add(new Tuple<>(ConversionUtils.newS(param.name.ident), ConversionUtils.newI(0)));
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
	
	private static boolean setContainsLabel(HashSet<Tuple<CopyStr, CopyInt>> set, String label) {
		for(Tuple<CopyStr, CopyInt> elem: set)
			if(elem.source.toString().equals(label))
				return true;
		return false;
	}
	
	private static Tuple<CopyStr, CopyInt> getLabel(HashSet<Tuple<CopyStr, CopyInt>> set, String label) {
		for(Tuple<CopyStr, CopyInt> elem: set)
			if(elem.source.toString().equals(label))
				return elem;
		return null;
	}

	@Override
	public HashSet<Tuple<CopyStr, CopyInt>> transferFunction(ICode instr, HashSet<Tuple<CopyStr, CopyInt>> inputSet) {
		HashSet<Tuple<CopyStr, CopyInt>> use = useSets.get(instr);
		HashSet<String> def = defSets.get(instr);
		HashSet<Tuple<CopyStr, CopyInt>> newSet = newSet();
		
		for(Tuple<CopyStr, CopyInt> in: inputSet)
			if(!def.contains(in.source.toString()))
				newSet.add(in.copy());
		
		HashSet<Tuple<CopyStr, CopyInt>> newish = newSet();
		for(Tuple<CopyStr, CopyInt> newElem: newSet)
			if(!setContainsLabel(use, newElem.source.toString()))
				newish.add(newElem.copy());
		
		for(Tuple<CopyStr, CopyInt> myUse: use) {
			newish.add(myUse.copy());
		}
		
		for(Tuple<CopyStr, CopyInt> tup: newish){
			tup.dest = ConversionUtils.newI(tup.dest.asInt() + 1);
		}
		
		return newish;
	}
	
	@Override
	public HashSet<Tuple<CopyStr, CopyInt>> performMeet(List<HashSet<Tuple<CopyStr, CopyInt>>> list) {
		HashSet<Tuple<CopyStr, CopyInt>> hash = newSet();
		
		for(HashSet<Tuple<CopyStr, CopyInt>> elem: list) {
			for(Tuple<CopyStr, CopyInt> myElem: elem)
				if(setContainsLabel(hash, myElem.source.toString())) {
					Tuple<CopyStr, CopyInt> source = getLabel(hash, myElem.source.toString()).copy();
					source.dest = ConversionUtils.newI((source.dest.asInt() + myElem.dest.asInt()) / 2);
					hash.add(source);
				} else {
					hash.add(new Tuple<>(myElem.source, myElem.dest));
				}
		}
		
		return hash;
	}
}
