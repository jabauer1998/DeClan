package io.github.h20man13.DeClan.common.arm.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.h20man13.DeClan.common.interfere.Color;
import io.github.h20man13.DeClan.common.interfere.InterferenceGraph;

public class ArmRegisterDescriptor implements Iterable<ArmRegisterElement>{
	private Map<ArmRegisterElement, Set<ArmAddressElement>> descriptorMap;
	private final Map<Integer, String> regNumberToIdent;
	
	
	public ArmRegisterDescriptor() {
		this.descriptorMap = new HashMap<ArmRegisterElement, Set<ArmAddressElement>>();
		this.descriptorMap.put(newRegElem("R0"), newSet());
        this.descriptorMap.put(newRegElem("R1"), newSet());
        this.descriptorMap.put(newRegElem("R2"), newSet());
        this.descriptorMap.put(newRegElem("R3"), newSet());
        this.descriptorMap.put(newRegElem("R4"), newSet());
        this.descriptorMap.put(newRegElem("R5"), newSet());
        this.descriptorMap.put(newRegElem("R6"), newSet());
        this.descriptorMap.put(newRegElem("R7"), newSet());
        this.descriptorMap.put(newRegElem("R8"), newSet());
        this.descriptorMap.put(newRegElem("R9"), newSet());
        this.descriptorMap.put(newRegElem("R10"), newSet());
        this.descriptorMap.put(newRegElem("R11"), newSet());
        this.descriptorMap.put(newRegElem("R12"), newSet());
        this.descriptorMap.put(newRegElem("R15"), newSet());
        
        this.regNumberToIdent = new HashMap<Integer, String>();
        this.regNumberToIdent.put(0, "R0");
        this.regNumberToIdent.put(1, "R1");
        this.regNumberToIdent.put(2, "R2");
        this.regNumberToIdent.put(3, "R3");
        this.regNumberToIdent.put(4, "R4");
        this.regNumberToIdent.put(5, "R5");
        this.regNumberToIdent.put(6, "R6");
        this.regNumberToIdent.put(7, "R7");
        this.regNumberToIdent.put(8, "R8");
        this.regNumberToIdent.put(9, "R9");
        this.regNumberToIdent.put(10, "R10");
        this.regNumberToIdent.put(11, "R11");
        this.regNumberToIdent.put(12, "R12");
        this.regNumberToIdent.put(13, "R15");
	}
	
	private ArmRegisterElement newRegElem(String name) {
		return new ArmRegisterElement(name);
	}
	
	public Set<ArmRegisterElement> getCandidateRegisters(){
		return this.descriptorMap.keySet();
	}
	
	public ArmRegisterDescriptor(Map<ArmRegisterElement, Set<ArmAddressElement>> newMap,
			Map<Integer, String> regNumberToIdent2) {
		this.descriptorMap = newMap;
		this.regNumberToIdent = regNumberToIdent2;
	}

	private Set<ArmAddressElement> newSet(){
		return new HashSet<ArmAddressElement>();
	}
	
	public void addAddressElement(ArmRegisterElement descriptor,  ArmAddressElement elem) {
		Set<ArmAddressElement> set = this.descriptorMap.get(descriptor);
		set.add(elem);
	}
	
	public Set<ArmAddressElement> getAddresses(ArmRegisterElement reg) {
		return this.descriptorMap.get(reg);
	}
	
	public void removeAddress(ArmRegisterElement reg, ArmAddressElement address){
		Set<ArmAddressElement> set = this.descriptorMap.get(reg);
		set.remove(address);
	}
	
	public void clearAddresses(ArmRegisterElement reg) {
		this.descriptorMap.put(reg, newSet());
	}

	@Override
	public Iterator<ArmRegisterElement> iterator() {
		return this.descriptorMap.keySet().iterator();
	}

	public ArmAddressElements getAddressesElements(ArmRegisterElement res) {
		return new ArmAddressElements(getAddresses(res));
	}

	public ArmRegisterDescriptor copy() {
		Map<ArmRegisterElement, Set<ArmAddressElement>> newMap = new HashMap<ArmRegisterElement, Set<ArmAddressElement>>();
		for(ArmRegisterElement key : this.descriptorMap.keySet()) {
			Set<ArmAddressElement> elems = this.descriptorMap.get(key);
			Set<ArmAddressElement> copy = new HashSet<ArmAddressElement>();
			for(ArmAddressElement elem: elems) {
				copy.add((ArmAddressElement)elem.copy());
			}
			newMap.put(key, copy);
		}
		return new ArmRegisterDescriptor(newMap, this.regNumberToIdent);
	}

	public boolean containsEmptyRegister() {
		for(ArmRegisterElement elem: this.descriptorMap.keySet()){
			Set<ArmAddressElement> elems = this.descriptorMap.get(elem);
			if(elems.isEmpty())
				return true;
		}
		return false;
	}

	public ArmRegisterElement getEmptyReg() {
		for(ArmRegisterElement elem: this.descriptorMap.keySet()){
			Set<ArmAddressElement> elems = this.descriptorMap.get(elem);
			if(elems.isEmpty())
				return elem;
		}
		throw new RuntimeException();
	}
	
	public int hashCode() {
		return Objects.hash(this.descriptorMap, this.regNumberToIdent);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ArmRegisterDescriptor) {
			ArmRegisterDescriptor other = (ArmRegisterDescriptor)obj;
			
			if((this.descriptorMap != null && other.descriptorMap == null)
			||(this.descriptorMap == null && other.descriptorMap != null))
				return false;
			
			if(this.descriptorMap.size() != other.descriptorMap.size())
				return false;
			
			return this.descriptorMap.equals(other.descriptorMap);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(ArmRegisterElement elem: this.descriptorMap.keySet()) {
			sb.append(elem.toString());
			sb.append("->");
			sb.append(this.descriptorMap.get(elem).toString());
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
