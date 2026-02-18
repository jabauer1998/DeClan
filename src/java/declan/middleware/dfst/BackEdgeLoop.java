package io.github.h20man13.DeClan.common.dfst;

import java.util.Iterator;
import java.util.List;

import io.github.h20man13.DeClan.common.flow.BlockNode;

public class BackEdgeLoop implements Iterable<BlockNode>{
	private List<BlockNode> loopElems;
	
	public BackEdgeLoop(List<BlockNode> loopElems) {
		this.loopElems = loopElems;
	}
	
	public boolean containsSubLoop(BackEdgeLoop loop) {
		for(BlockNode loopElem: loop){
			if(!loopElems.contains(loopElem))
				return false;
		}
		return true;
	}

	@Override
	public Iterator<BlockNode> iterator() {
		return loopElems.iterator();
	}
	
	
}
