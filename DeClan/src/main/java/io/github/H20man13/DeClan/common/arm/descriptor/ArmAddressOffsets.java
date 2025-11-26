package io.github.H20man13.DeClan.common.arm.descriptor;

import java.lang.StackWalker.StackFrame;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import io.github.H20man13.DeClan.common.Tuple;
import io.github.H20man13.DeClan.common.exception.OffsetNotFound;
import io.github.H20man13.DeClan.common.icode.ICode;
import io.github.H20man13.DeClan.common.icode.ICode.Type;

public class ArmAddressOffsets{
	private Stack<OffsetFrame> frameStack;
	
	public ArmAddressOffsets() {
		this.frameStack = new Stack<OffsetFrame>();
	}
	
	public void pushFrame() {
		if(frameStack.isEmpty())
			frameStack.push(new OffsetFrame(0));
		
		OffsetFrame top = frameStack.peek();
		int off = top.getNextOffset();
		frameStack.push(new OffsetFrame(off));
	}
	
	public void pushAddress(String elem, ICode.Type type) {
		ArmAddressElement myElem = new ArmAddressElement(elem, type);
		pushAddress(myElem, type);
	}
	
	public void pushAddress(ArmAddressElement elem, ICode.Type type){
		Set<ArmAddressElement> setElems = new HashSet<ArmAddressElement>();
		setElems.add(elem);
		ArmAddressElements armElems = new ArmAddressElements(setElems);
		pushAddress(armElems, type);
	}
	
	public void pushAddress(ArmAddressElements addrs, ICode.Type type) {
		OffsetFrame frame = frameStack.peek();
		int offset = frame.getNextOffset();
		int myOffset = frame.pushAddress(addrs, type);
		for(OffsetFrame myFrame: this.frameStack) {
			if(myFrame != frame)
				myFrame.addOffset(myOffset);
		}
	}
	
	public int findOffset(String elem, ICode.Type type) {
		ArmAddressElement element = new ArmAddressElement(elem, type);
		return findOffset(element);
	}
	
	public int findOffset(ArmAddressElement elem){
		for(int i = frameStack.size() - 1; i >= 0; i--) {
			OffsetFrame frame = frameStack.get(i);
			if(frame.containsOffsetForAnyElem(elem)){
				return frame.findOffsetForAnyElem(elem);
			}
		}
		throw new OffsetNotFound("Offset for " + elem + " not found in stack");
	}
	
	public boolean containsOffset(String strelem, ICode.Type type) {
		ArmAddressElement elem = new ArmAddressElement(strelem, type);
		return containsOffset(elem);
	}
	
	public boolean containsOffset(ArmAddressElement elem) {
		for(int i = 0; i >= 0; i--){
			OffsetFrame frame = frameStack.get(i);
			if(frame.containsOffsetForAnyElem(elem))
				return true;
		}
		return false;
	}

	public void popFrame() {
		this.frameStack.pop();
		int toSub = this.frameStack.peek().getNextOffset();
		for(OffsetFrame frame: frameStack){
			frame.subtractOffset(toSub);
		}
	}

}
