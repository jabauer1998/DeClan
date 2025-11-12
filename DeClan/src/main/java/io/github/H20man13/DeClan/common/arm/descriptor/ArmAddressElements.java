package io.github.H20man13.DeClan.common.arm.descriptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ArmAddressElements implements Iterable<ArmAddressElement> {
	private Set<ArmAddressElement> elems;
	
	public ArmAddressElements(Set<ArmAddressElement> elems) {
		this.elems = elems;
	}
	
	public boolean containsElem(ArmAddressElement elem) {
		return this.elems.contains(elem);
	}
	
	public boolean containsElem(String str) {
		return this.elems.contains(new ArmAddressElement(str));
	}
	
	public boolean containsAllElem(Collection<ArmAddressElement> col){
		return this.elems.containsAll(col);
	}

	public boolean containsAnyElem(ArmAddressElements elems2) {
		for(ArmAddressElement elem: elems2) {
			if(this.containsElem(elem))
				return true;
		}
		return false;
	}

	@Override
	public Iterator<ArmAddressElement> iterator() {
		return elems.iterator();
	}
}
