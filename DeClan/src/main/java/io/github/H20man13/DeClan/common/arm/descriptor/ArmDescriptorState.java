package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.H20man13.DeClan.common.CopyStr;
import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Type;
import io.github.H20man13.DeClan.common.icode.Spill;
import io.github.H20man13.DeClan.common.icode.exp.BinExp.Operator;

public class ArmDescriptorState implements Copyable<ArmDescriptorState> {
	private ArmRegisterDescriptor regDesc;
	private ArmAddressDescriptor addrDesc;
	private List<Tuple<CopyStr, ICode.Type>> spill;
	
	private static CopyStr newS(String s) {
		return new CopyStr(s);
	}
	
	public ArmDescriptorState(){
		this.regDesc = new ArmRegisterDescriptor();
		this.addrDesc = new ArmAddressDescriptor();
		this.spill = new LinkedList<Tuple<CopyStr, ICode.Type>>();
	}
	
	private ArmDescriptorState(ArmRegisterDescriptor regDesc, ArmAddressDescriptor addrDesc){
		this.regDesc = regDesc;
		this.addrDesc = addrDesc;
		this.spill = new LinkedList<Tuple<CopyStr, ICode.Type>>();
	}
	
	public void addRegValuePair(String ident, ICode.Type type,  String reg){
		ArmRegisterElement regElem = new ArmRegisterElement(reg);
		ArmAddressElement addrElem = new ArmAddressElement(ident, type);
		this.regDesc.addAddressElement(regElem, addrElem);
		this.addrDesc.addRegister(addrElem, regElem);
	}
	
	public boolean containsPlaceInReg(String a, ICode.Type type){
		return this.addrDesc.containedInRegisters(new ArmAddressElement(a, type));
	}
	
	public boolean containsEmptyReg(){
		return this.regDesc.containsEmptyRegister();
	}
	
	public String pickEmptyReg() {
		return this.regDesc.getEmptyReg().toString();
	}
	
	public HashSet<String> getCanditateRegs(){
		HashSet<String> strElems = new HashSet<String>();
		Set<ArmRegisterElement> elems = this.regDesc.getCandidateRegisters();
		for(ArmRegisterElement elem: elems) {
			strElems.add(elem.toString());
		}
		return strElems;
	}
	
	public void removeRegFromAddress(String reg, String addr, ICode.Type type){
		this.addrDesc.removeRegister(new ArmAddressElement(addr, type), new ArmRegisterElement(reg));
	}
	
	public Set<Tuple<CopyStr, Type>> getCandidateAddresses(String reg){
		HashSet<Tuple<CopyStr, ICode.Type>> addrsRet = new HashSet<Tuple<CopyStr, ICode.Type>>();
		Set<ArmAddressElement> addrs = regDesc.getAddresses(new ArmRegisterElement(reg));
		for(ArmAddressElement elem: addrs) {
			addrsRet.add(elem.toTuple());
		}
		return addrsRet;
	}
	
	public void clearRegisterAddresses(String reg){
		regDesc.clearAddresses(new ArmRegisterElement(reg));
	}
	
	public Set<String> getRegistersAssociatedWithPlace(String place, ICode.Type type){
		Set<ArmRegisterElement> elem = this.addrDesc.getRegisters(new ArmAddressElement(place, type));
		HashSet<String> regs = new HashSet<String>();
		for(ArmRegisterElement elems: elem) {
			regs.add(elems.toString());
		}
		return regs;
	}
	
	@Override
	public ArmDescriptorState copy() {
		return new ArmDescriptorState(regDesc.copy(), addrDesc.copy());
	}

	public boolean isContainedInMoreThenOnePlace(String addr, ICode.Type type) {
		return addrDesc.getElements(new ArmAddressElement(addr, type)).size() > 1;
	}

	public void addSpill(String ident, Type i) {
		this.spill.add(new Tuple<>(newS(ident), i));
	}

	public boolean containsOnlyPlaceInAReg(String place, ICode.Type type) {
		for(ArmRegisterElement elem: regDesc.getCandidateRegisters()) {
			Set<ArmAddressElement> elems = regDesc.getAddresses(elem);
			if(elems.size() == 1 && elems.contains(new ArmAddressElement(place, type)))
				return true;
		}
		return false;
	}

	public boolean containsSpill() {
		return !this.spill.isEmpty();
	}

	public List<Tuple<CopyStr, ICode.Type>> getSpill() {
		return this.spill;
	}
	
	public int hashCode() {
		return Objects.hash(this.spill, regDesc, addrDesc);
	}
	
	public boolean equals(Object obj){
		if(obj instanceof ArmDescriptorState) {
			ArmDescriptorState other = (ArmDescriptorState)obj;
			if(this.spill.equals(other.spill))
				if(this.regDesc.equals(other.regDesc))
					return this.addrDesc.equals(other.addrDesc);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Register Descriptor: \n");
		sb.append("---------------------\n");
		sb.append(this.regDesc.toString());
		sb.append("---------------------\n");
		sb.append("AddressDescriptor: \n");
		sb.append("---------------------\n");
		sb.append(this.addrDesc.toString());
		sb.append("---------------------\n");
		sb.append("Spills: \n");
		sb.append("---------------------\n");
		sb.append(this.spill.toString());
		return sb.toString();
	}
}
