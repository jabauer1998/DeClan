package io.github.h20man13.DeClan.common.dfst;

import io.github.h20man13.DeClan.common.Tuple;
import io.github.h20man13.DeClan.common.flow.BlockNode;

public class DepthFirstMetaEdge extends Tuple<RootDfstNode, RootDfstNode> {
	public DepthFirstMetaEdge(RootDfstNode source, RootDfstNode dest) {
		super(source, dest);
	}
	
	public DepthFirstMetaEdge(Tuple<RootDfstNode, RootDfstNode> source) {
		super(source.source, source.dest);
	}
	
	public DepthFirstMetaEdge(DepthFirstMetaEdge edge) {
		super(edge.source, edge.dest);
	}
}
