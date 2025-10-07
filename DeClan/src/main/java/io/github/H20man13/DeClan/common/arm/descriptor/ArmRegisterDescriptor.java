package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ArmRegisterDescriptor implements Iterable<ArmRegisterElement>{
	private Map<ArmRegisterElement, Set<ArmAddressElement>> descriptorMap;
	
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
	}
	
	private Set<ArmAddressElement> newSet(){
		return new HashSet<ArmAddressElement>();
	}
	
	private ArmRegisterElement newRegElem(String name) {
		return new ArmRegisterElement(name);
	}
	
	private ArmAddressElement newAddElem(String name) {
		return new ArmAddressElement(name);
	}
	
	public void addAddressElement(String descriptor, String elem) {
		Set<ArmAddressElement> set = this.descriptorMap.get(newRegElem(descriptor));
		set.add(newAddElem(elem));
	}
	
	public Set<ArmAddressElement> getAddresses(String reg) {
		return this.descriptorMap.get(newRegElem(reg));
	}
	
	public void removeAddress(String reg, String address){
		Set<ArmAddressElement> set = this.descriptorMap.get(newRegElem(reg));
		set.remove(newAddElem(address));
	}
	
	public void clearAddresses(String reg) {
		this.descriptorMap.put(newRegElem(reg), newSet());
	}

	@Override
	public Iterator<ArmRegisterElement> iterator() {
		return this.descriptorMap.keySet().iterator();
	}
}
