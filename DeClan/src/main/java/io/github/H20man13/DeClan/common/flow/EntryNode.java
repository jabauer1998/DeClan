package io.github.H20man13.DeClan.common.flow;

import javax.swing.RowFilter.Entry;

public class EntryNode implements DagNode {
    private DagNode entry;

    public EntryNode(DagNode entry){
        this.entry = entry;   
    }
}
