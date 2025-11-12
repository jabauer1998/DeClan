package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArmAddressDescriptor {
	private Map<ArmAddressElement, Set<ArmElement>> discriptorMap;
	
	public ArmAddressDescriptor() {
		this.discriptorMap = new HashMap<ArmAddressElement, Set<ArmElement>>();
	}
	
	private Set<ArmElement> newSet() {
		return new HashSet<ArmElement>();
	}
	
	private ArmRegisterElement newRegElem(String name) {
		return new ArmRegisterElement(name);
	}
	
	private ArmAddressElement newAddElem(String name) {
		return new ArmAddressElement(name);
	}
	
	public Set<ArmRegisterElement> getRegisters(String address) {
		ArmAddressElement addr = newAddElem(address);
		return getRegisters(addr);
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
	
	public Set<ArmElement> getElements(String address) {
		ArmAddressElement addr = newAddElem(address);
		return getElements(addr);
	}
	
	public Set<ArmElement> getElements(ArmAddressElement addr) {
		HashSet<ArmElement> regs = new HashSet<ArmElement>();
		if(discriptorMap.containsKey(addr))
			for(ArmElement elem: discriptorMap.get(addr)) {
				regs.add(elem);
			}
		return regs;
	}
	
	public void addAddress(String address, String toAdd) {
		ArmAddressElement addr = newAddElem(address);
		if(!discriptorMap.containsKey(addr))
			discriptorMap.put(addr, newSet());
		
		Set<ArmElement> elem = discriptorMap.get(addr);
		elem.add(newAddElem(toAdd));
	}
	
	public void addRegister(String address, String toAdd) {
		ArmAddressElement addr = newAddElem(address);
		if(!discriptorMap.containsKey(addr))
			discriptorMap.put(addr, newSet());
		
		Set<ArmElement> elem = discriptorMap.get(addr);
		elem.add(newRegElem(toAdd));
	}
	
	public void removeAddress(String address, String toRemove) {
		ArmAddressElement addr = newAddElem(address);
		Set<ArmElement> elem = discriptorMap.get(addr);
		elem.remove(newAddElem(toRemove));
		
		if(elem.isEmpty())
			discriptorMap.remove(addr);
	}
	
	public void removeRegister(String address, String toRemove) {
		ArmAddressElement addr = newAddElem(address);
		Set<ArmElement> elem = discriptorMap.get(addr);
		elem.remove(newRegElem(toRemove));
		
		if(elem.isEmpty())
			discriptorMap.remove(addr);
	}
}
