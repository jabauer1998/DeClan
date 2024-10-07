package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashSet;
import java.util.Set;

import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;

public class DominatorAnalysis extends BasicBlockAnalysis<FlowGraphNode> {
	public DominatorAnalysis(FlowGraph flowGraph) {
		super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION);
	}

	@Override
	public Set<FlowGraphNode> transferFunction(FlowGraphNode instr, Set<FlowGraphNode> inputSet) {
		Set<FlowGraphNode> toRet = new HashSet<FlowGraphNode>();
		toRet.addAll(inputSet);
		toRet.add(instr);
		return toRet;
	}
}
