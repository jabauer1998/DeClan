package io.github.h20man13.DeClan.common.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.h20man13.DeClan.common.Config;
import io.github.h20man13.DeClan.common.analysis.AnalysisBase.Direction;
import io.github.h20man13.DeClan.common.analysis.AnalysisBase.Meet;
import io.github.h20man13.DeClan.common.flow.FlowGraph;
import io.github.h20man13.DeClan.common.flow.FlowGraphNode;
import io.github.h20man13.DeClan.common.util.Utils;

public class DominatorAnalysis extends BasicBlockAnalysis<HashMap<FlowGraphNode, HashSet<FlowGraphNode>>, HashSet<FlowGraphNode>, FlowGraphNode> {
	public DominatorAnalysis(FlowGraph flowGraph, Config cfg) {
		super(flowGraph, Direction.FORWARDS, Meet.INTERSECTION, false, cfg, Utils.getClassType(HashMap.class), Utils.getClassType(HashSet.class));
	}

	@Override
	public HashSet<FlowGraphNode> transferFunction(FlowGraphNode instr, HashSet<FlowGraphNode> inputSet) {
		HashSet<FlowGraphNode> toRet = newSet();
		toRet.addAll(inputSet);
		toRet.add(instr);
		return toRet;
	}
}
