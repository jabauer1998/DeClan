package io.github.h20man13.DeClan.common.arm.descriptor;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.exception.NoDataFoundInFrameException;
import io.github.h20man13.DeClan.common.icode.ICode;

public class OffsetFrame {
	private Stack<ArmAddressElements> elementStack;
	private Stack<Integer> offSet;
	private Stack<Integer> size;
	private Integer offset;
	
	public OffsetFrame(int offset) {
		elementStack = new Stack<ArmAddressElements>();
		offSet = new Stack<Integer>();
		size = new Stack<Integer>();
		this.offset = offset;
	}
	
	public void pushAddress(ArmAddressElement elem, int offset, ICode.Type type) {
		Set<ArmAddressElement> myElems = new HashSet<ArmAddressElement>();
		myElems.add(elem);
		ArmAddressElements myActualElems = new ArmAddressElements(myElems);
		Stack<Integer> reverseStack = new Stack<Integer>();
		while(!offSet.isEmpty()) {
			Integer myElem = offSet.pop();
			reverseStack.push(myElem);
		}
		int incr = -1;
		switch(type) {
		case INT:
			incr = 4;
			break;
		case REAL:
			incr = 4;
			break;
		case BOOL:
			incr = 1;
			break;
		case STRING:
			incr = 4;
			break;
		default:
			throw new RuntimeException("Error malformed stack frame");
		}
		
		while(!reverseStack.isEmpty()) {
			Integer myElem = reverseStack.pop();
			offSet.push(myElem + incr);
		}
		
		elementStack.push(myActualElems);
		offSet.push(0);
		switch(type) {
		case INT:
			size.push(4);
			break;
		case REAL:
			size.push(4);
			break;
		case BOOL:
			size.push(1);
			break;
		case STRING:
			size.push(4);
			break;
		default:
			throw new RuntimeException("Error malformed stack frame");
		}
	}
	
	public int pushAddress(ArmAddressElements elems, ICode.Type type) {
		Stack<Integer> reverseStack = new Stack<Integer>();
		while(!offSet.isEmpty()) {
			Integer myElem = offSet.pop();
			reverseStack.push(myElem);
		}
		int incr = -1;
		switch(type) {
		case INT:
			incr = 4;
			break;
		case REAL:
			incr = 4;
			break;
		case BOOL:
			incr = 1;
			break;
		case STRING:
			incr = 4;
			break;
		default:
			throw new RuntimeException("Error malformed stack frame");
		}
		
		while(!reverseStack.isEmpty()) {
			Integer myElem = reverseStack.pop();
			offSet.push(myElem + incr);
		}
		
		elementStack.push(elems);
		offSet.push(0);
		switch(type) {
		case INT:
			size.push(4);
			return 4;
		case REAL:
			size.push(4);
			return 4;
		case BOOL:
			size.push(1);
			return 1;
		case STRING:
			size.push(4);
			return 4;
		default:
			throw new RuntimeException("Error malformed stack frame");
		}
	}
	
	public int getNextOffset() {
		if(offSet.empty() && size.empty())
			return offset;
		return offSet.peek() + size.peek();
	}
	
	public int findOffsetForAnyElems(ArmAddressElements elems) {
		for(int i = elementStack.size() - 1; i >= 0; i--) {
			ArmAddressElements elem = elementStack.get(i);
			if(elem.containsAnyElem(elems))
				return offSet.get(i) + size.get(i);
		}
		
		throw new NoDataFoundInFrameException("Addr(" + elems + ") not found!!");
	}
	
	public int findOffsetForAnyElem(ArmAddressElement addr) {
		Set<ArmAddressElement> myElems = new HashSet<ArmAddressElement>();
		myElems.add(addr);
		ArmAddressElements myActualElems = new ArmAddressElements(myElems);
		return findOffsetForAnyElems(myActualElems);
	}
	
	public boolean containsOffsetForAnyElems(ArmAddressElements elems){
		for(int i = elementStack.size() - 1; i >= 0; i--) {
			ArmAddressElements elem = elementStack.get(i);
			if(elem.containsAnyElem(elems))
				return true;
		}
		
		return false;
	}

	public boolean containsOffsetForAnyElem(ArmAddressElement addr) {
		Set<ArmAddressElement> myElems = new HashSet<ArmAddressElement>();
		myElems.add(addr);
		ArmAddressElements myActualElems = new ArmAddressElements(myElems);
		return containsOffsetForAnyElems(myActualElems);
	}
	
	public int findOffsetForAllElems(ArmAddressElements elems) {
		for(int i = elementStack.size() - 1; i >= 0; i--) {
			ArmAddressElements elem = elementStack.get(i);
			if(elem.equals(elems)){
				return offSet.get(i) + size.get(i);
			}
		}
		
		throw new NoDataFoundInFrameException("Addr(" + elems + ") not found!!");
	}
	
	public int findOffsetForAllElem(ArmAddressElement addr) {
		Set<ArmAddressElement> myElems = new HashSet<ArmAddressElement>();
		myElems.add(addr);
		ArmAddressElements myActualElems = new ArmAddressElements(myElems);
		return findOffsetForAllElems(myActualElems);
	}
	
	public void addOffset(int toAdd) {
		Stack<Integer> st = new Stack<Integer>();
		while(!offSet.isEmpty()){
			st.push(offSet.pop());
		}
		while(!st.isEmpty()){
			offSet.push(st.pop() + toAdd);
		}
		this.offset += toAdd;
	}
	
	public void subtractOffset(int toSub) {
		Stack<Integer> st = new Stack<Integer>();
		while(!offSet.isEmpty()){
			st.push(offSet.pop());
		}
		while(!st.isEmpty()){
			offSet.push(st.pop() - toSub);
		}
	}
	
	public boolean containsOffsetForAllElems(ArmAddressElements elems){
		for(int i = elementStack.size() - 1; i >= 0; i--) {
			ArmAddressElements elem = elementStack.get(i);
			if(elem.equals(elems)){
				return true;
			}
		}
		
		return false;
	}

	public boolean containsOffsetForAllElem(ArmAddressElement addr) {
		Set<ArmAddressElement> myElems = new HashSet<ArmAddressElement>();
		myElems.add(addr);
		ArmAddressElements myActualElems = new ArmAddressElements(myElems);
		return containsOffsetForAllElems(myActualElems);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Frame offset=");
		sb.append(offset);
		sb.append("\n");
		for(int i = elementStack.size() - 1; i >= 0; i--){
			sb.append("Elem " + elementStack.get(i));
			sb.append(" at offset ");
			sb.append(offSet.get(i));
			sb.append(" with size " + size.get(i));
			sb.append("\n");
			if(i > 0) {
				sb.append("|\nV\n");
			}
		}
		return sb.toString();
	}
}
