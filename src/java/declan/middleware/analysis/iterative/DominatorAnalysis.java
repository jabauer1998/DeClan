package declan.middleware.analysis.iterative;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import declan.driver.Config;
import declan.middleware.analysis.AnalysisBase.Direction;
import declan.middleware.analysis.AnalysisBase.Meet;
import declan.utils.flow.FlowGraph;
import declan.utils.flow.FlowGraphNode;
import declan.utils.Utils;

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
