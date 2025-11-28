package io.github.H20man13.DeClan.common.flow;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.github.H20man13.DeClan.common.Copyable;
import io.github.H20man13.DeClan.common.icode.ICode;

public interface FlowGraphNode extends Copyable<FlowGraphNode>{
    public List<ICode> getICode();
    @Override
    public String toString();
	FlowGraphNode copy();
}
