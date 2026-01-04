package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Type;

public class ArmAddressDescriptor implements Copyable<ArmAddressDescriptor> {
	private Map<ArmAddressElement, Set<ArmElement>> discriptorMap;
	
	public ArmAddressDescriptor() {
		this.discriptorMap = new HashMap<ArmAddressElement, Set<ArmElement>>();
	}
	
	private ArmAddressDescriptor(Map<ArmAddressElement, Set<ArmElement>> myMap){
		this.discriptorMap = myMap;
	}
	
	public Set<ArmAddressElement> getAddressKeys() {
		return discriptorMap.keySet();
	}
	
	private Set<ArmElement> newSet() {
		return new HashSet<ArmElement>();
	}
	
	public int hashCode() {
		return this.discriptorMap.hashCode();
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof ArmAddressDescriptor) {
			ArmAddressDescriptor other = (ArmAddressDescriptor)obj;
			
			if((this.discriptorMap == null && other.discriptorMap != null)
			|| (this.discriptorMap != null && other.discriptorMap == null))
				return false;
				
			if(this.discriptorMap.size() != other.discriptorMap.size())
				return false;
			
			return this.discriptorMap.equals(other.discriptorMap);
		}
		return false;
	}
	
	public Set<ArmRegisterElement> getRegisters(ArmAddressElement elem){
		HashSet<ArmRegisterElement> regs = new HashSet<ArmRegisterElement>();
		if(discriptorMap.containsKey(elem))
			for(ArmElement myElem: discriptorMap.get(elem)) {
				if(myElem instanceof ArmRegisterElement){
					regs.add((ArmRegisterElement)myElem);
				}
			}
		return regs;
	}
	
	public boolean containedInRegisters(ArmAddressElement elem){
		HashSet<ArmRegisterElement> regs = new HashSet<ArmRegisterElement>();
		if(discriptorMap.containsKey(elem))
			for(ArmElement myElem: discriptorMap.get(elem)) {
				if(myElem instanceof ArmRegisterElement){
					return true;
				}
			}
		return false;
	}
	
	public Set<ArmElement> getElements(ArmAddressElement addr) {
		HashSet<ArmElement> regs = new HashSet<ArmElement>();
		if(discriptorMap.containsKey(addr))
			for(ArmElement elem: discriptorMap.get(addr)) {
				regs.add(elem);
			}
		return regs;
	}
	
	public void addAddress(ArmAddressElement addr, ArmAddressElement elem){
		if(!discriptorMap.containsKey(addr))
			discriptorMap.put(addr, newSet());
		
		Set<ArmElement> elems = discriptorMap.get(addr);
		elems.add(elem);
	}
	
	public void addRegister(ArmAddressElement address, ArmRegisterElement toAdd) {
		if(!discriptorMap.containsKey(address))
			discriptorMap.put(address, newSet());
		
		Set<ArmElement> elem = discriptorMap.get(address);
		elem.add(toAdd);
	}
	
	public void removeAddress(ArmAddressElement address, ArmAddressElement toRemove) {
		Set<ArmElement> elem = discriptorMap.get(address);
		elem.remove(toRemove);
		
		if(elem.isEmpty())
			discriptorMap.remove(address);
	}
	
	public void removeRegister(ArmAddressElement address, ArmRegisterElement toRemove) {
		Set<ArmElement> elem = discriptorMap.get(address);
		elem.remove(toRemove);
		
		if(elem.isEmpty())
			discriptorMap.remove(address);
	}

	public ArmAddressDescriptor copy() {
		Map<ArmAddressElement, Set<ArmElement>> newMap = new HashMap<>();
		for(ArmAddressElement elem: discriptorMap.keySet()){
			HashSet<ArmElement> mySet = new HashSet<ArmElement>();
			for(ArmElement elems: discriptorMap.get(elem)) {
				mySet.add(elems.copy());
			}
			newMap.put(elem, mySet);
		}
		return new ArmAddressDescriptor(newMap);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(ArmAddressElement elem: this.discriptorMap.keySet()) {
			sb.append(elem.toString());
			sb.append("->");
			sb.append(this.discriptorMap.get(elem).toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public Set<ArmAddressElement> getAddresses(ArmAddressElement armAddressElement) {
		Set<ArmAddressElement> mySet = new HashSet<ArmAddressElement>();
		for(ArmElement elem: this.discriptorMap.get(armAddressElement))
			if(elem instanceof ArmAddressElement)
				mySet.add((ArmAddressElement)elem);
		return mySet;
	}

	public void addAddressPair(ArmAddressElement armAddressElement, ArmAddressElement armAddressElement2) {
		if(!this.discriptorMap.containsKey(armAddressElement))
			this.discriptorMap.put(armAddressElement, newSet());
			
		Set<ArmElement> elems = this.discriptorMap.get(armAddressElement);
		elems.add(armAddressElement2);
	}
}
