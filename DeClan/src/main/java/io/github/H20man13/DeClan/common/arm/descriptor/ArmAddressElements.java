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
	
	@Override
	public String toString() {
		return elems.toString();
	}
	
	@Override
	public int hashCode() {
		return elems.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ArmAddressElements) {
			ArmAddressElements myElems = (ArmAddressElements)obj;
			if(myElems.elems.size() != elems.size())
				return false;
			
			return myElems.elems.equals(elems);
		}
		return false;
	}
}
