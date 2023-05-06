package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.icode.BasicBlock;

public interface FlowGraphNode {
    public void removeDeadCode();
    public Set<Set<FlowGraphNode>> identifyLoops(Set<FlowGraphNode> visited);
    public boolean containsPredecessorOutsideLoop(Set<FlowGraphNode> loop);
}
