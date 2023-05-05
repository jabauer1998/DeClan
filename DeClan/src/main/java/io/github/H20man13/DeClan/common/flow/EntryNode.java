package io.github.H20man13.DeClan.common.flow;

import javax.swing.RowFilter.Entry;

public class EntryNode implements FlowGraphNode {
    private FlowGraphNode entry;

    public EntryNode(FlowGraphNode entry){
        this.entry = entry;   
    }
}
