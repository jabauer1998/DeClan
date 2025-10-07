package io.github.H20man13.DeClan.common.arm;

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
import io.github.H20man13.DeClan.common.arm.descriptor.ArmElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterDescriptor;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterElement;
import io.github.H20man13.DeClan.common.arm.descriptor.ArmRegisterResult;
import io.github.H20man13.DeClan.common.exception.RegisterAllocatorException;
import io.github.H20man13.DeClan.common.icode.Assign;
import io.github.H20man13.DeClan.common.icode.Def;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.exp.BinExp;
import io.github.H20man13.DeClan.common.icode.exp.IdentExp;
import io.github.H20man13.DeClan.common.icode.exp.UnExp;

public class ArmRegisterGenerator {
    private LiveVariableAnalysis liveAnal;
    private ArmRegisterDescriptor registersDescriptor;
    private ArmAddressDescriptor addressDescriptor;

    public ArmRegisterGenerator(LiveVariableAnalysis liveAnal){
        this.liveAnal = liveAnal;
        this.registersDescriptor = new ArmRegisterDescriptor();
        this.addressDescriptor = new ArmAddressDescriptor();
    }
    
    private Map<String, String> searchForRegs(String def, String... strings) {
    	Map<String, String> res = new HashMap<String, String>();
    	
    	Set<ArmAddressElement> myAdderesses = new HashSet<ArmAddressElement>();
    	for(String s: strings){
    		myAdderesses.add(new ArmAddressElement(s));
    	}
    	
    	loop: for(ArmAddressElement myElem: myAdderesses){
    		for(ArmRegisterElement descriptor: this.registersDescriptor) {
    			Set<ArmAddressElement> elem = this.registersDescriptor.getAddresses(descriptor.toString());
    			if(elem.contains(myElem) && !res.keySet().contains(myElem.toString())) {
    				res.put(myElem.toString(), descriptor.toString());
    				continue loop;
    			}
    		}
    		for(ArmRegisterElement descriptor: this.registersDescriptor) {
    			Set<ArmAddressElement> elem = this.registersDescriptor.getAddresses(descriptor.toString());
    			if(elem.isEmpty()) {
    				res.put(myElem.toString(), descriptor.toString());
    				this.registersDescriptor.addAddressElement(descriptor.toString(), myElem.toString());
    				this.addressDescriptor.addRegister(myElem.toString(), descriptor.toString());
    			}
    		}
    	}
    }

    private ArmRegisterResult getRegs(ICode instruction){
        if(instruction instanceof Assign) {
        	Assign ass = (Assign)instruction;
        	if(ass.value instanceof BinExp) {
        		BinExp bin = (BinExp)ass.value;
        		String left = bin.left.ident;
        		String right = bin.right.ident;
        		String newIsh = ass.place;
        		
        		searchForRegs(newIsh, left, right);
        	} else if(ass.value instanceof UnExp) {
        		
        	} else if(ass.value instanceof IdentExp) {
        		
        	}
        } else if(instruction instanceof Def){
        	Def def = (Def)instruction;
        	if(def.val instanceof BinExp){
        		
        	} else if(def.val instanceof UnExp) {
        		
        	} else if(def.val instanceof IdentExp) {
        		
        	}
        }
    }
}
