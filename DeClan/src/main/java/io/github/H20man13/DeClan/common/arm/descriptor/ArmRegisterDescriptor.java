package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.github.H20man13.DeClan.common.interfere.Color;
import io.github.H20man13.DeClan.common.interfere.InterferenceGraph;

public class ArmRegisterDescriptor implements Iterable<ArmRegisterElement>{
	private Map<ArmRegisterElement, Set<ArmAddressElement>> descriptorMap;
	private Map<Integer, String> regNumberToIdent;
	private Map<String, Color> colorGraph;
	
	public ArmRegisterDescriptor(Map<String, Color> colorGraph) {
		this.colorGraph = colorGraph;
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
	
	private Color getColor(String name) {
		return this.colorGraph.get(name);
	}
	
	private static int convertToRawInt(Color col) {
		return col.literalColor();
	}
	
	private String getReg(int color) {
		return this.regNumberToIdent.get(color);
	}

	public ArmRegisterElement getEmptyReg(String name) {
		Color col = getColor(name);
		int intCol = convertToRawInt(col);
		String actualReg = getReg(intCol);
		return new ArmRegisterElement(actualReg);
	}
	
	public Set<ArmAddressElement> getAddresses(ArmRegisterElement elem){
		return this.descriptorMap.get(elem);
	}

	public ArmAddressElements getAddressesElements(ArmRegisterElement res) {
		return new ArmAddressElements(getAddresses(res));
	}
}
