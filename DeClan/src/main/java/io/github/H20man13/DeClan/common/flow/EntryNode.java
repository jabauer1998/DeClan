package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.Set;

import javax.swing.RowFilter.Entry;

public class EntryNode implements FlowGraphNode {
    private FlowGraphNode entry;

    public EntryNode(FlowGraphNode entry){
        this.entry = entry;   
    }

    @Override
    public void removeDeadCode() {
        entry.removeDeadCode();
    }

    public Set<Set<FlowGraphNode>> identifyLoops(){
        return this.identifyLoops(new HashSet<FlowGraphNode>());        
    }

    @Override
    public Set<Set<FlowGraphNode>> identifyLoops(Set<FlowGraphNode> visited) {
        return entry.identifyLoops(visited);
    }

    @Override
    public boolean containsPredecessorOutsideLoop(Set<FlowGraphNode> loop) {
        return false;
    }


}
