package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.RowFilter.Entry;

import io.github.H20man13.DeClan.common.icode.ICode;

public class EntryNode implements FlowGraphNode {
    private FlowGraphNode entry;

    public EntryNode(FlowGraphNode entry){
        this.entry = entry;   
    }

    @Override
    public void removeDeadCode() {
        entry.removeDeadCode();
    }

    @Override
    public Set<Set<FlowGraphNode>> identifyLoops(Set<FlowGraphNode> visited) {
        return entry.identifyLoops(visited);
    }

    @Override
    public boolean containsPredecessorOutsideLoop(Set<FlowGraphNode> loop) {
        return false;
    }

    @Override
    public void generateOptimizedIr() {}

    @Override
    public List<ICode> getICode() {
        return new LinkedList<ICode>();
    }


}
