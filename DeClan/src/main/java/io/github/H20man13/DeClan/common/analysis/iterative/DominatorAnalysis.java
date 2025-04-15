package io.github.H20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.H20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.H20man13.DeClan.common.flow.FlowGraph;
import io.github.H20man13.DeClan.common.flow.FlowGraphNode;
import io.github.H20man13.DeClan.common.util.Utils;

public class DominatorAnalysis extends BasicBlockAnalysis<HashMap<FlowGraphNode, HashSet<FlowGraphNode>>, HashSet<FlowGraphNode>, FlowGraphNode> {
	public DominatorAnalysis(FlowGraph flowGraph) {
		super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, false, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
	}

	@Override
	public HashSet<FlowGraphNode> transferFunction(FlowGraphNode instr, HashSet<FlowGraphNode> inputSet) {
		HashSet<FlowGraphNode> toRet = newSet();
		toRet.addAll(inputSet);
		toRet.add(instr);
		return toRet;
	}
}
