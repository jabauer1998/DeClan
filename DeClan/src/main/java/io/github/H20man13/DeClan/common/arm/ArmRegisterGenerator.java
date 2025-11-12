package io.github.H20man13.DeClan.common.arm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.analysis.iterative.LiveVariableAnalysis;
import io.github.H20man13.DeClan.common.arm.ArmCodeGenerator.VariableLength;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressDescriptor;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressElements;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmAddressOffsets;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterDescriptor;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterResult;
import io.github.H20man13.DeClan.common.exception.NoSpillFoundException;
import io.github.H20man13.DeClan.common.exception.RegisterAllocatorException;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.Lib;
import io.github.H20man13.DeClan.common.icode.Prog;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;
import io.github.H20man13.DeClan.common.icode.inline.Inline;
import io.github.H20man13.DeClan.common.interfere.Color;
import io.github.H20man13.DeClan.common.interfere.InterferenceGraph;

public class ArmRegisterGenerator {
    private LiveVariableAnalysis liveAnal;
    private ArmRegisterDescriptor registersDescriptor;
    private ArmAddressDescriptor addressDescriptor;
    private ArmAddressOffsets offsets;
    private ArmCodeGenerator gen;
    private HashSet<String> registersNotInUse = new HashSet<String>();
    private Set<ArmAddressElement> spillSet = new HashSet<ArmAddressElement>();

    public ArmRegisterGenerator(Lib lib, LiveVariableAnalysis liveAnal, ArmAddressOffsets offsets, ArmCodeGenerator gen){
        this.liveAnal = liveAnal;
        this.offsets = offsets;
        this.gen = gen;
        InterferenceGraph graph = new InterferenceGraph(lib, liveAnal);
        Tuple<Map<String, Color>, Set<String>> coloredGraph = graph.colorGraph();
        Set<String> mySpill = coloredGraph.dest;
        this.spillSet = new HashSet<ArmAddressElement>();
        for(String spVal : mySpill) {
        	spillSet.add(new ArmAddressElement(spVal));
        }
        this.registersDescriptor = new ArmRegisterDescriptor(coloredGraph.source);
        this.addressDescriptor = new ArmAddressDescriptor();
    }
    
    private boolean allAddressesExistElsewhere(Set<ArmAddressElement> elems){
    	for(ArmAddressElement v: elems){
			Set<ArmElement> myElements = this.addressDescriptor.getElements(v);
			if(!(myElements.size() > 1))
				return false;
		}
    	return true;
    }
    
    private boolean elemsAreBeingComputedAndNotAnOperand(Set<ArmAddressElement> elems, IdentExp defIdent, IdentExp... strings){
    	for(ArmAddressElement elem: elems){
    		if(!elem.toString().equals(defIdent.ident)){
    			return false;
    		} else {
    			for(IdentExp exp: strings){
    				if(elem.toString().equals(exp.ident)){
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
    
    private boolean elemsAreAllDead(Set<ArmAddressElement> elems, HashSet<String> out) {
    	for(ArmAddressElement v: elems) {
    		if(out.contains(v.toString())) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private ArmRegisterResult selectRegs(HashSet<String> areAlive, IdentExp def, ICode.Type defType, IdentExp[] strings, ICode.Type[] types) {
    	ArmRegisterResult result = new ArmRegisterResult();
    	
    	for(int i = 0; i < strings.length; i++) {
    		IdentExp ident = strings[i];
    		ICode.Type type = types[i];
    		Set<ArmRegisterElement> elem = this.addressDescriptor.getRegisters(ident.ident);
    		if(!elem.isEmpty()){
    			ArmRegisterElement myElem = (ArmRegisterElement)elem.toArray()[0];
    			result.addResult(ident, myElem.toString());
        	} else {
        		ArmRegisterElement emptyReg = this.registersDescriptor.getEmptyReg(ident.ident);
        		if (emptyReg != null) {
        			result.addResult(ident, emptyReg.toString());
    				this.registersDescriptor.addAddressElement(emptyReg.toString(), ident.ident);
    				this.addressDescriptor.addRegister(ident.ident, emptyReg.toString());
        		} else if(emptyReg == null) {
        			ArrayList<Tuple<ArmRegisterResult, Integer>> arrList = new ArrayList<Tuple<ArmRegisterResult, Integer>>();
        			for(ArmRegisterElement reg: this.registersDescriptor) {
        				Set<ArmAddressElement> elems = this.registersDescriptor.getAddresses(reg.toString());
        				if(allAddressesExistElsewhere(elems)){
        					result.addResult(ident, reg.toString());
        					this.registersDescriptor.clearAddresses(reg.toString());
        					this.registersDescriptor.addAddressElement(reg.toString(), ident.ident);
        					this.addressDescriptor.addRegister(ident.ident, reg.toString());
        				} else if(elemsAreBeingComputedAndNotAnOperand(elems, def, strings)) {
        					result.addResult(ident, reg.toString());
        					this.registersDescriptor.clearAddresses(reg.toString());
        					this.registersDescriptor.addAddressElement(reg.toString(), ident.ident);
        					this.addressDescriptor.addRegister(ident.ident, reg.toString());
        				} else if(elemsAreAllDead(elems, areAlive)){
        					result.addResult(ident, reg.toString());
        					this.registersDescriptor.clearAddresses(reg.toString());
        					this.registersDescriptor.addAddressElement(reg.toString(), ident.ident);
        					this.addressDescriptor.addRegister(ident.ident, reg.toString());
        				} else {
        					ArmRegisterElement res = chooseRegToSpill(areAlive);
        					ArmAddressElements addrs = this.registersDescriptor.getAddressesElements(res);
        					this.offsets.pushAddress(addrs, type);
        					this.registersDescriptor.clearAddresses(reg.toString());
        					this.registersDescriptor.addAddressElement(reg.toString(), ident.ident);
        					this.addressDescriptor.addRegister(ident.ident, reg.toString());
        				}
        			}
        		}
        	}
    	}
    	return result;
    }
    
    private ArmRegisterElement chooseRegToSpill(HashSet<String> areAlive){
    	for(ArmRegisterElement elem: this.registersDescriptor){
    		Set<ArmAddressElement> addrs = this.registersDescriptor.getAddresses(elem.toString());
    		
    		Set<ArmAddressElement> myAddrs = new HashSet<ArmAddressElement>();
    		myAddrs.addAll(addrs);
    		
    		Set<ArmAddressElement> inty = new HashSet<ArmAddressElement>();
    		inty.addAll(spillSet);
    		inty.retainAll(addrs);
    		
    		myAddrs.removeAll(inty);
    		
    		if(myAddrs.isEmpty())
    			return elem;
    		else if(elemsAreAllDead(myAddrs, areAlive))
    			return elem;
    	}
    	
    	throw new NoSpillFoundException("Error no suitable register found for spilling");
    }

    public ArmRegisterResult getRegs(ICode instruction){
    	HashSet<String> strs = this.liveAnal.getOutputSet(instruction);
        if(instruction instanceof Assign) {
        	Assign ass = (Assign)instruction;
        	if(ass.value instanceof BinExp) {
        		BinExp bin = (BinExp)ass.value;
        		String newIsh = ass.place;
        		switch (bin.op) {
        			case BEQ: return selectRegs(strs, new IdentExp(ass.getScope(), ass.place), ass.getType(), new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.BOOL, ICode.Type.BOOL});
        			case BNE: return selectRegs(strs, new IdentExp(ass.getScope(), ass.place), ass.getType(), new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.BOOL, ICode.Type.BOOL});
        			default: return selectRegs(strs, new IdentExp(ass.getScope(), ass.place), ass.getType(), new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.INT, ICode.Type.INT});
        		}
        	} else if(ass.value instanceof UnExp) {
        		UnExp un = (UnExp)ass.value;
        		String dest = ass.place;
        		return selectRegs(strs, new IdentExp(ass.getScope(), dest), ass.getType(), new IdentExp[] { un.right }, new ICode.Type[] { ass.getType() });
        	} else if(ass.value instanceof IdentExp) {
        		IdentExp exp = (IdentExp)ass.value;
        		String dest = ass.place;
        		return selectRegs(strs, new IdentExp(ass.getScope(), ass.place), ass.getType(), new IdentExp[] {exp}, new ICode.Type[] {ass.getType()});
        	}
        } else if(instruction instanceof Def){
        	Def def = (Def)instruction;
        	if(def.val instanceof BinExp) {
        		BinExp bin = (BinExp)def.val;
        		String newIsh = def.label;
        		switch (bin.op) {
        			case BEQ: return selectRegs(strs, new IdentExp(def.scope, def.label), def.type, new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.BOOL, ICode.Type.BOOL});
        			case BNE: return selectRegs(strs, new IdentExp(def.scope, def.label), def.type, new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.BOOL, ICode.Type.BOOL});
        			default: return selectRegs(strs, new IdentExp(def.scope, def.label), def.type, new IdentExp[]{bin.left, bin.right}, new ICode.Type[]{ICode.Type.INT, ICode.Type.INT});
        		}
        	} else if(def.val instanceof UnExp) {
        		UnExp un = (UnExp)def.val;
        		String dest = def.label;
        		return selectRegs(strs, new IdentExp(def.scope, dest), def.type, new IdentExp[] { un.right }, new ICode.Type[] { def.type });
        	} else if(def.val instanceof IdentExp) {
        		IdentExp exp = (IdentExp)def.val;
        		String dest = def.label;
        		return selectRegs(strs, new IdentExp(def.scope, def.label), def.type, new IdentExp[] {exp}, new ICode.Type[] {def.type});
        	}
        }
        return new ArmRegisterResult();
    }
}
