package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.RowFilter.Entry;

import io.github.H20man13.DeClan.common.icode.ICode;

public class EntryNode implements FlowGraphNode {
    public FlowGraphNode entry;

    public EntryNode(FlowGraphNode entry){
        this.entry = entry;   
    }

    @Override
    public List<ICode> getICode() {
        return new LinkedList<ICode>();
    }

    @Override
    public List<ICode> getAllICode(){
        return new LinkedList<ICode>();
    }

    @Override
    public String toString(){
        return "ENTRY";
    }
}
